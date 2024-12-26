package com.example.demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

public class HelloApplication extends Application {

    private final Map<String, Integer> cartCalories = new HashMap<>();
    private final Map<String, List<String>> categories = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Recipe Book");

        // Категории и рецепты
        categories.put("Завтрак", Arrays.asList("Омлет", "Блины", "Смузи"));
        categories.put("Обед", Arrays.asList("Паста Болоньезе", "Борщ", "Курица с овощами"));
        categories.put("Десерт", Arrays.asList("Шоколадный торт", "Чизкейк", "Мороженое"));

        // Компоненты интерфейса
        ListView<String> recipeListView = new ListView<>();
        recipeListView.getStyleClass().add("recipe-list");

        // Создаем ImageView для отображения изображения блюда
        ImageView recipeImageView = new ImageView();
        recipeImageView.setFitWidth(200);
        recipeImageView.setFitHeight(150);
        recipeImageView.setPreserveRatio(true);

        // Создаем TextFlow для отображения деталей рецепта
        TextFlow recipeDetailsFlow = new TextFlow();
        recipeDetailsFlow.setPrefWidth(300);
        recipeDetailsFlow.setStyle("-fx-padding: 10px; -fx-border-color: gray;");

        // Создаем TextFlow для отображения ингредиентов
        TextFlow ingredientsFlow = new TextFlow();
        ingredientsFlow.setPrefWidth(300);
        ingredientsFlow.setStyle("-fx-padding: 10px; -fx-border-color: gray;");

        // Поле ввода для порций
        TextField servingsField = new TextField();
        servingsField.setPromptText("Введите порции");
        servingsField.getStyleClass().add("servings-field");


        Button addToCartButton = new Button("Добавить в корзину");
        addToCartButton.getStyleClass().add("cart-button");


        recipeListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Обновляем описание рецепта с изображением и текстом
                recipeDetailsFlow.getChildren().clear();
                recipeDetailsFlow.getChildren().add(new Text(getRecipeDetails(newValue)));
                Image recipeImage = getRecipeImage(newValue);
                if (recipeImage != null) {
                    recipeDetailsFlow.getChildren().add(new ImageView(recipeImage));
                }
                // Обновляем ингредиенты
                ingredientsFlow.getChildren().clear();
                ingredientsFlow.getChildren().add(new Text(getIngredients(newValue)));
            }
        });

        updateRecipeList(recipeListView, "Завтрак"); // Начальная категория

        // Панель с информацией о рецепте
        VBox recipePanel = new VBox(10, new Label("Рецепты:"), recipeListView, servingsField, addToCartButton,
                new Label("Описание:"), recipeDetailsFlow, new Label("Ингредиенты:"), ingredientsFlow, recipeImageView);
        recipePanel.setPadding(new Insets(10));
        recipePanel.getStyleClass().add("recipe-panel");

        // Список корзины
        ListView<HBox> cartListView = new ListView<>();
        cartListView.getStyleClass().add("cart-list");

        // Метка для калорийности корзины
        Label cartCaloriesLabel = new Label("Общая калорийность: 0 ккал");
        cartCaloriesLabel.getStyleClass().add("cart-calories-label");

        // Панель с кнопками категорий
        VBox categoryButtons = new VBox(10);
        categoryButtons.setPadding(new Insets(10));
        Button breakfastButton = new Button("Завтрак");
        Button lunchButton = new Button("Обед");
        Button dessertButton = new Button("Десерт");

        breakfastButton.getStyleClass().add("category-button");
        lunchButton.getStyleClass().add("category-button");
        dessertButton.getStyleClass().add("category-button");

        categoryButtons.getChildren().addAll(breakfastButton, lunchButton, dessertButton);

        // Обработчики кнопок категорий
        breakfastButton.setOnAction(event -> updateRecipeList(recipeListView, "Завтрак"));
        lunchButton.setOnAction(event -> updateRecipeList(recipeListView, "Обед"));
        dessertButton.setOnAction(event -> updateRecipeList(recipeListView, "Десерт"));

        // Добавление в корзину
        addToCartButton.setOnAction(event -> {
            String selectedRecipe = recipeListView.getSelectionModel().getSelectedItem();
            if (selectedRecipe != null) {
                try {
                    int servings = Integer.parseInt(servingsField.getText());
                    if (servings > 0) {
                        if (!cartCalories.containsKey(selectedRecipe)) {
                            CheckBox checkBox = new CheckBox(selectedRecipe + " - " + servings + " порций");
                            HBox cartItem = new HBox(10, checkBox);
                            cartListView.getItems().add(cartItem);
                            cartCalories.put(selectedRecipe, servings);
                        } else {
                            showAlert(Alert.AlertType.WARNING, "Рецепт уже добавлен в корзину!");
                        }
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Введите положительное число порций!");
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.WARNING, "Введите корректное число порций!");
                }
            }
        });

        // Очистка корзины
        Button clearCartButton = new Button("Очистить корзину");
        clearCartButton.getStyleClass().add("cart-button");
        clearCartButton.setOnAction(event -> {
            cartListView.getItems().clear();
            cartCalories.clear();
            cartCaloriesLabel.setText("Общая калорийность: 0 ккал");
        });

        // Удаление выбранных элементов
        Button removeSelectedButton = new Button("Удалить выбранные");
        removeSelectedButton.getStyleClass().add("cart-button");
        removeSelectedButton.setOnAction(event -> {
            Iterator<HBox> iterator = cartListView.getItems().iterator();
            while (iterator.hasNext()) {
                HBox cartItem = iterator.next();
                for (javafx.scene.Node node : cartItem.getChildren()) {
                    if (node instanceof CheckBox) {
                        CheckBox checkBox = (CheckBox) node;
                        if (checkBox.isSelected()) {
                            String recipeName = checkBox.getText().split(" - ")[0];
                            cartCalories.remove(recipeName);
                            iterator.remove();
                        }
                    }
                }
            }
        });

        // Расчет калорийности
        Button calculateCartCaloriesButton = new Button("Рассчитать калории корзины");
        calculateCartCaloriesButton.getStyleClass().add("cart-button");
        calculateCartCaloriesButton.setOnAction(event -> {
            int totalCalories = 0;
            for (HBox cartItem : cartListView.getItems()) {
                for (javafx.scene.Node node : cartItem.getChildren()) {
                    if (node instanceof CheckBox) {
                        CheckBox checkBox = (CheckBox) node;
                        String recipeName = checkBox.getText().split(" - ")[0];
                        int servings = cartCalories.get(recipeName);
                        totalCalories += calculateCalories(recipeName, servings);
                    }
                }
            }
            cartCaloriesLabel.setText("Общая калорийность: " + totalCalories + " ккал");
        });

        // Панели
        VBox cartPanel = new VBox(10, new Label("Корзина:"), cartListView, clearCartButton, removeSelectedButton, calculateCartCaloriesButton, cartCaloriesLabel);
        cartPanel.setPadding(new Insets(10));
        cartPanel.getStyleClass().add("cart-panel");

        // Основной макет
        BorderPane root = new BorderPane();
        root.setLeft(categoryButtons);
        root.setCenter(recipePanel);
        root.setRight(cartPanel);
        root.setPadding(new Insets(10));
        root.getStyleClass().add("root-pane");

        // Сцена
        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/Styles/recipe.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateRecipeList(ListView<String> recipeListView, String category) {
        recipeListView.getItems().clear();
        recipeListView.getItems().addAll(categories.get(category));
    }

    private String getRecipeDetails(String recipeName) {
        return "Это описание рецепта для " + recipeName + ". Простой, вкусный и полезный!";
    }

    private Image getRecipeImage(String recipeName) {
        // Путь к изображению в папке resources/images
        String imagePath = "/images/" + recipeName + ".jpg";

        // Загрузка изображения
        URL imageUrl = getClass().getResource(imagePath);
        if (imageUrl != null) {
            return new Image(imageUrl.toExternalForm());
        } else {
            System.out.println("Изображение не найдено: " + recipeName);
            return null; // Если изображение не найдено, возвращаем null
        }
    }


    private String getIngredients(String recipeName) {
        switch (recipeName) {
            case "Омлет":
                return "Ингредиенты для Омлета: яйца, молоко, масло, соль, перец.";
            case "Блины":
                return "Ингредиенты для Блинов: мука, яйца, молоко, сахар, соль, растительное масло.";
            case "Смузи":
                return "Ингредиенты для Смузи: бананы, клубника, молоко, мед.";
            case "Паста Болоньезе":
                return "Ингредиенты для Пасты Болоньезе: паста, мясной фарш, томатный соус, лук, чеснок, оливковое масло.";
            case "Борщ":
                return "Ингредиенты для Борща: капуста, картофель, свекла, морковь, лук, мясо, томатная паста.";
            case "Курица с овощами":
                return "Ингредиенты для Курицы с овощами: куриное филе, картофель, морковь, лук, чеснок, специи.";
            case "Шоколадный торт":
                return "Ингредиенты для Шоколадного торта: мука, яйца, сахар, какао, сливочное масло.";
            case "Чизкейк":
                return "Ингредиенты для Чизкейка: творог, сахар, яйца, печенье, сливочное масло.";
            case "Мороженое":
                return "Ингредиенты для Мороженого: сливки, молоко, сахар, ваниль.";
            default:
                return "Ингредиенты не найдены.";
        }
    }

    private int calculateCalories(String recipeName, int servings) {
        switch (recipeName) {
            case "Омлет":
                return 150 * servings;  // Примерная калорийность омлета (150 ккал на порцию)
            case "Блины":
                return 200 * servings;  // Примерная калорийность блинов (200 ккал на порцию)
            case "Смузи":
                return 120 * servings;  // Примерная калорийность смузи (120 ккал на порцию)
            case "Паста Болоньезе":
                return 350 * servings;  // Примерная калорийность пасты Болоньезе (350 ккал на порцию)
            case "Борщ":
                return 250 * servings;  // Примерная калорийность борща (250 ккал на порцию)
            case "Курица с овощами":
                return 300 * servings;  // Примерная калорийность курицы с овощами (300 ккал на порцию)
            case "Шоколадный торт":
                return 400 * servings;  // Примерная калорийность шоколадного торта (400 ккал на порцию)
            case "Чизкейк":
                return 350 * servings;  // Примерная калорийность чизкейка (350 ккал на порцию)
            case "Мороженое":
                return 180 * servings;  // Примерная калорийность мороженого (180 ккал на порцию)
            default:
                return 0;  // Если рецепт не найден, возвращаем 0 калорий
        }
    }


    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}