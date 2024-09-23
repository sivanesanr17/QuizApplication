import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.List;
public class LoginApp extends Application {
    private MongoDatabase database;
    @Override
    public void start(Stage primaryStage) {
        database = MongoClients.create("mongodb://localhost:27017").getDatabase("loginSystem");
        primaryStage.setTitle("Quiz Application");
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setPadding(new Insets(20));
        Text welcomeText = new Text("Welcome to the Quiz Application");
        welcomeText.setFont(Font.font("Poppins", 20));
        Label usernameLabel = new Label("Username:");
        usernameLabel.setFont(Font.font("Poppins", 10));
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("Poppins", 10));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        Hyperlink signUpLink = new Hyperlink("or Sign Up?");
        signUpLink.setFont(Font.font("Poppins"));
        signUpLink.setStyle("-fx-text-fill: black;");
        anchorPane.getChildren().addAll(welcomeText, usernameLabel, usernameField, passwordLabel, passwordField, loginButton, signUpLink);
        AnchorPane.setTopAnchor(welcomeText, 15.0);
        AnchorPane.setLeftAnchor(welcomeText, 50.0);
        AnchorPane.setTopAnchor(usernameLabel, 80.0);
        AnchorPane.setLeftAnchor(usernameLabel, 50.0);
        AnchorPane.setTopAnchor(usernameField, 65.0);
        AnchorPane.setLeftAnchor(usernameField, 150.0);
        AnchorPane.setRightAnchor(usernameField, 50.0);
        AnchorPane.setTopAnchor(passwordLabel, 140.0);
        AnchorPane.setLeftAnchor(passwordLabel, 50.0);
        AnchorPane.setTopAnchor(passwordField, 120.0);
        AnchorPane.setLeftAnchor(passwordField, 150.0);
        AnchorPane.setRightAnchor(passwordField, 50.0);
        AnchorPane.setTopAnchor(loginButton, 180.0);
        AnchorPane.setLeftAnchor(loginButton, 190.0);
        AnchorPane.setTopAnchor(signUpLink, 220.0);
        AnchorPane.setLeftAnchor(signUpLink, 190.0);
        Scene scene = new Scene(anchorPane, 465, 300);
        scene.getStylesheets().add("color.css");
        primaryStage.setScene(scene);
        primaryStage.show();
        signUpLink.setOnAction(e -> openSignUpWindow());
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter both username and password");
            } else {
                if (username.equals("Admin") && password.equals("AdminPassword")) {
                    openAdminPanel();
                } else {
                    if (validateUserCredentials(username, password)) {
                        openUserPanel(username);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password");
                    }
                }
            }
        });
    }
    private void openSignUpWindow() {
        Stage signUpStage = new Stage();
        signUpStage.setTitle("Sign Up");
        VBox signUpBox = new VBox(10);
        signUpBox.setPadding(new Insets(20));
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        Label retypePasswordLabel = new Label("Retype Password:");
        PasswordField retypePasswordField = new PasswordField();
        retypePasswordField.setPromptText("Retype your password");
        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        signUpBox.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, retypePasswordLabel, retypePasswordField, registerButton);
        Scene signUpScene = new Scene(signUpBox, 300, 350);
        signUpScene.getStylesheets().add("color.css"); 
        signUpStage.setScene(signUpScene);
        signUpStage.show();
        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String retypePassword = retypePasswordField.getText();
            if (username.isEmpty() || password.isEmpty() || retypePassword.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "All fields are required.");
            } else if (!password.equals(retypePassword)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match.");
            } else {
                registerUser(username, password);
                showAlert(Alert.AlertType.INFORMATION, "Success", "User registered successfully.");
                signUpStage.close();
            }
        });
    }
    private void registerUser(String username, String plainPassword) {
        MongoCollection<Document> usersCollection = database.getCollection("users");
        Document existingUser = usersCollection.find(new Document("username", username)).first();
        if (existingUser != null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User already exists.");
            return;
        }
        Document newUser = new Document("username", username)
                .append("password", plainPassword);  
        usersCollection.insertOne(newUser);
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); 
        alert.setContentText(message);
    
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("color.css").toExternalForm());  
        dialogPane.getStyleClass().add("custom-alert");  
        
        alert.showAndWait();
    }
    private boolean validateUserCredentials(String username, String password) {
        MongoCollection<Document> usersCollection = database.getCollection("users");
        Document user = usersCollection.find(new Document("username", username)).first();
        if (user != null) {
            String storedPassword = user.getString("password");
            return password.equals(storedPassword);  
        }
        return false;
    }
    private ObjectId currentQuizId = null;  
    private void openAdminPanel() {
        Stage adminStage = new Stage();
        adminStage.setTitle("Admin Panel - Manage Quizzes");
        VBox adminBox = new VBox(10);
        adminBox.setPadding(new Insets(20));
        Label titleLabel = new Label("Admin Panel - Add or Delete Quizzes");
        TextField quizTitleField = new TextField();
        quizTitleField.setPromptText("Quiz Title");
        Button addQuizButton = new Button("Add Quiz");
        addQuizButton.setOnAction(e -> addQuiz(quizTitleField.getText()));
        TextField questionField = new TextField();
        questionField.setPromptText("Question");
        questionField.setStyle("-fx-font-size: 12px;"); 
        TextField option1Field = new TextField();
        option1Field.setPromptText("Option 1");
        option1Field.setStyle("-fx-font-size: 12px;");
        TextField option2Field = new TextField();
        option2Field.setPromptText("Option 2");
        option2Field.setStyle("-fx-font-size: 12px;");
        TextField option3Field = new TextField();
        option3Field.setPromptText("Option 3");
        option3Field.setStyle("-fx-font-size: 12px;");
        TextField option4Field = new TextField();
        option4Field.setPromptText("Option 4");
        option4Field.setStyle("-fx-font-size: 12px;");
        TextField correctAnswerField = new TextField();
        correctAnswerField.setPromptText("Correct Answer");
        correctAnswerField.setStyle("-fx-font-size: 12px;");
        Button addQuestionButton = new Button("Add Question");
        addQuestionButton.setStyle("-fx-font-size: 12px;");
        addQuestionButton.setOnAction(e -> {
            if (currentQuizId != null) {
                addQuestion(currentQuizId, questionField.getText(), option1Field.getText(),
                        option2Field.getText(), option3Field.getText(), option4Field.getText(), correctAnswerField.getText());
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Please add a quiz before adding questions.");
            }
        });
        TextField quizDeleteField = new TextField();
        quizDeleteField.setPromptText("Quiz Title to Delete");
        quizDeleteField.setStyle("-fx-font-size: 12px;");
        Button deleteQuizButton = new Button("Delete Quiz");
        deleteQuizButton.setStyle("-fx-font-size: 12px;");
        deleteQuizButton.setOnAction(e -> deleteQuizByTitle(quizDeleteField.getText()));
        adminBox.getChildren().addAll(titleLabel, quizTitleField, addQuizButton, questionField, option1Field, option2Field,
        option3Field, option4Field, correctAnswerField, addQuestionButton, quizDeleteField, deleteQuizButton);
        Scene adminScene = new Scene(adminBox, 300, 615);
        adminScene.getStylesheets().add("color.css");
        adminStage.setScene(adminScene);
        adminStage.show();
    }
    private void addQuiz(String quizTitle) {
        MongoCollection<Document> quizzesCollection = database.getCollection("quizzes");
        Document existingQuiz = quizzesCollection.find(new Document("title", quizTitle)).first();
        if (existingQuiz != null) {
            currentQuizId = existingQuiz.getObjectId("_id");
            showAlert(Alert.AlertType.INFORMATION, "Quiz Exists", "Quiz already exists. You can now add questions to it.");
        } else {
            Document newQuiz = new Document("title", quizTitle).append("questions", new ArrayList<>()); 
            quizzesCollection.insertOne(newQuiz);  
            currentQuizId = newQuiz.getObjectId("_id");  
            showAlert(Alert.AlertType.INFORMATION, "Success", "Quiz added successfully.");
        }
    }
    private void addQuestion(ObjectId quizId, String question, String option1, String option2, String option3, String option4, String correctAnswer) {
        MongoCollection<Document> quizzesCollection = database.getCollection("quizzes");
        List<String> options = new ArrayList<>();
        options.add(option1);
        options.add(option2);
        options.add(option3);
        options.add(option4);
        Document newQuestion = new Document("question", question).append("options", options).append("correctAnswer", correctAnswer);
        quizzesCollection.updateOne(new Document("_id", quizId),new Document("$push", new Document("questions", newQuestion)));
        showAlert(Alert.AlertType.INFORMATION, "Success", "Question added successfully.");
    }
    private void deleteQuizByTitle(String quizTitle) {
        MongoCollection<Document> quizzesCollection = database.getCollection("quizzes");
        quizzesCollection.deleteOne(new Document("title", quizTitle));
        showAlert(Alert.AlertType.INFORMATION, "Success", "Quiz deleted successfully.");
    }
    private void openUserPanel(String username) {
        Stage userStage = new Stage();
        userStage.setTitle("User Panel - " + username);
        VBox userBox = new VBox(10);
        userBox.setPadding(new Insets(20));
        Label welcomeLabel = new Label("Welcome, " + username + "!");
        welcomeLabel.setFont(Font.font("Poppins", 20));
        ComboBox<String> quizComboBox = new ComboBox<>();
        quizComboBox.setPromptText("Select a Quiz");
        loadAvailableQuizzes(quizComboBox);
        Button startQuizButton = new Button("Take Quiz");
        startQuizButton.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        startQuizButton.setOnAction(e -> {
            String selectedQuiz = quizComboBox.getValue();
            if (selectedQuiz != null) {
                loadQuizQuestions(selectedQuiz, username);
            } else {
                showAlert(Alert.AlertType.ERROR, "No Quiz Selected", "Please select a quiz.");
            }
        });
        userBox.getChildren().addAll(welcomeLabel, quizComboBox, startQuizButton);
        Scene userScene = new Scene(userBox, 400, 200);
        userScene.getStylesheets().add("color.css"); 
        userStage.setScene(userScene);
        userStage.show();
    }
    private void loadAvailableQuizzes(ComboBox<String> quizComboBox) {
        MongoCollection<Document> quizzesCollection = database.getCollection("quizzes");
        for (Document quiz : quizzesCollection.find()) {
            quizComboBox.getItems().add(quiz.getString("title"));
        }
    }
    private void loadQuizQuestions(String quizTitle, String username) {
        MongoCollection<Document> quizzesCollection = database.getCollection("quizzes");
        Document quiz = quizzesCollection.find(new Document("title", quizTitle)).first();
        if (quiz != null) {
            List<Document> questions = quiz.getList("questions", Document.class);
            if (!questions.isEmpty()) {
                showQuizQuestions(questions, username);
            } else {
                showAlert(Alert.AlertType.ERROR, "No Questions", "This quiz has no questions.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Quiz Not Found", "Could not find the selected quiz.");
        }
    }
    private void showQuizQuestions(List<Document> questions, String username) {
        int totalQuestions = questions.size();
        int[] currentIndex = {0}; 
        int[] score = {0}; 
        showQuestionWindow(questions, currentIndex, score, username, totalQuestions);
    }
    private void showQuestionWindow(List<Document> questions, int[] currentIndex, int[] score, String username, int totalQuestions) {
        if (currentIndex[0] < questions.size()) {
            Document questionDoc = questions.get(currentIndex[0]);
            String question = questionDoc.getString("question");
            List<String> options = questionDoc.getList("options", String.class);
            String correctAnswer = questionDoc.getString("correctAnswer");
            Stage questionStage = new Stage();
            questionStage.setTitle("Question " + (currentIndex[0] + 1));
            VBox questionBox = new VBox(10);
            questionBox.setPadding(new Insets(20));
            Label questionLabel = new Label(question);
            questionLabel.getStyleClass().add("question-label");
            questionBox.getChildren().add(questionLabel);
            ToggleGroup group = new ToggleGroup();
            for (String option : options) {
                RadioButton optionButton = new RadioButton(option);
                optionButton.setToggleGroup(group);
                optionButton.getStyleClass().add("option-button"); 
                questionBox.getChildren().add(optionButton);
            }
            Button nextButton = new Button(currentIndex[0] == totalQuestions - 1 ? "Submit" : "Next");
            nextButton.getStyleClass().add("next-button");            nextButton.setOnAction(e -> {
                RadioButton selectedOption = (RadioButton) group.getSelectedToggle();
                if (selectedOption != null && selectedOption.getText().equals(correctAnswer)) {
                    score[0]++;
                }
                questionStage.close();  
                if (currentIndex[0] == totalQuestions - 1) {
                    saveQuizResult(username, score[0], totalQuestions);  
                } else {
                    currentIndex[0]++; 
                    showQuestionWindow(questions, currentIndex, score, username, totalQuestions); 
                }
            });
            questionBox.getChildren().add(nextButton);
            Scene questionScene = new Scene(questionBox, 600, 250);
            questionScene.getStylesheets().add("color.css"); 
            questionStage.setScene(questionScene);
            questionStage.show();
        }
    }
    private void saveQuizResult(String username, int score, int totalQuestions) {
        MongoCollection<Document> resultsCollection = database.getCollection("quizResults");
        Document result = new Document("username", username).append("score", score).append("totalQuestions", totalQuestions).append("date", new java.util.Date());
        resultsCollection.insertOne(result);
        showFinalScore(score, totalQuestions, username);
    }
    private void showFinalScore(int score, int totalQuestions, String username) {
        Stage resultStage = new Stage();
        resultStage.setTitle("Quiz Result");
        VBox resultBox = new VBox(10);
        resultBox.setPadding(new Insets(20));
        Label resultLabel = new Label(username + ", your final score is " + score + " out of " + totalQuestions);
        resultLabel.setFont(Font.font("Poppins", 18));
        resultBox.getChildren().add(resultLabel);
        Scene resultScene = new Scene(resultBox, 400, 80);
        resultStage.setScene(resultScene);
        resultStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
