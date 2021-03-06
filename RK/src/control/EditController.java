package control;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.Addresses;
import model.AlertBox;
import model.ConfirmBox;
import model.ConfirmCancelBox;
import model.Constants;
import model.Ingredient;
import model.IngredientException;
import model.ReadData;
import model.Recipe;
import model.RecipeList;
import model.WriteData;

/**
 * EditController initializes when user chooses to create a new recipe
 * or choose to edit a recipe being read
 * In edit mode, user can modify an existed recipe and save it, or save as their own version
 * edit view can be switched between read, main view or other edit view 
 * (Ex: user chooses to create a new recipe in edit mode of an existed recipe)
 * 
 * Most of the implementations for buttons that switch scenes are explained from line 318-358:
 * 	(menuNew, backward, forward, home)
 * @author Hoa Pham
 *
 */
public class EditController implements Initializable{
	@FXML // fx:id="motherPane"
	private BorderPane motherPane;

	@FXML // fx:id="recipeName"
	private TextField recipeName; 

	@FXML // fx:id="ingreName"
	private TextField ingreName;

	@FXML // fx:id="ingreQty"
	private TextField ingreQty;

	@FXML // fx:id="textCategory"
	private TextField textCategory;

	@FXML //fx:id="instructions"
	private TextArea instructions;

//	@FXML // fx:id="servingSize"
//	private ComboBox<String> servingSize;

	@FXML // fx:id="ingreUnit"
	private ComboBox<String> ingreUnit;

	@FXML // fx:id="menuNew"
	private MenuItem menuNew;						// New...			

	@FXML // fx:id="menuEdit"
	private MenuItem menuEdit;						// Edit

	@FXML // fx:id="menuSave"
	private MenuItem menuSave;						// Save

	@FXML // fx:id="menuSaveAs"
	private MenuItem menuSaveAs;						// Save As...

	@FXML // fx:id="backward"
	private Button backward;							// <

	@FXML // fx:id"forward"
	private Button forward;							// >

	@FXML // fx:id="home"
	private Button home;								// home (???)

	@FXML // fx:id="addIngredient"
	private Button addIngredient;					// "+" button for ingredients

	@FXML // fx:id="delIngredient"
	private Button delIngredient;					// "-" button for ingredients

	@FXML // fx:id="addCategory"
	private Button addCategory;						// "+" button for categories

	@FXML // fx:id="delCategory"
	private Button delCategory;						// "-" button for categories

	@FXML // fx:id="ingredientsTable"
	private TableView<Ingredient> ingredientsTable;

	@FXML // fx:id="categoryTable"
	private ListView<String> categoryTable;
	private ObservableList<String> categories = FXCollections.observableArrayList();

	// Recipe chose from model
	private Recipe recipe = new Recipe();

	// previous recipe from reading view
	private Recipe recipeFromRead = new Recipe();

	// Another Recipe used to check if user hit save button
	private Recipe oldRep = new Recipe();

	// constants
	private static Constants constants = new Constants();

	// list of units
	private final String[] UNITS = constants.getUnits();
	// serving Sizes
	//private final String[] SERVSIZES = constants.getServsizes();
	// minimum size of the window
	private final int[] MIN_SIZES = constants.getMinSizes();

	/**
	 * user's accessing history
	 */
	private Addresses history = new Addresses();
	
	// Data passed from welcome screen
	private ArrayList<Recipe> readingData;

	/**
	 * temporary ingredient list
	 * will be added to part of a new recipe if the user wants to
	 */
	ObservableList<Ingredient> ingredients = FXCollections.observableArrayList();

	/**
	 * Quantity per servingSize: created when an ingredient is added
	 * used for changing servingSize. Ex: when user enter (chicken, 1, lb)
	 * qtyPerServingSize will add 1 
	 */
//	List<Double> qtyPerServingSize = new ArrayList<Double>();

	public EditController() {
		super();
	}

	/**
	 * Constructor
	 * @param r
	 */
	public EditController(Recipe r) {
		initData(r);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// Event listener when name box is changed
		recipeName.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
				recipe.setName(newValue);
			}
		});

		// instructions TextArea
		instructions.setWrapText(true);
		// Event listener when instructions box is changed
		instructions.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
				recipe.setInstructions(newValue);
			}
		});

		// menuNew listener
		menuNew.setOnAction(new EventHandler<ActionEvent>() {
			@Override 
			public void handle(ActionEvent e) {
//				if (recipe.compareTo(oldRep) != 1) {
//					int clickResult = ConfirmCancelBox.display("Notice", "Do you want to make changes to " + recipeName.getText() + "?"
//							+ "\n" + "Your change(s) will be lost if you don't save them.");
//					switch(clickResult) {
//					case(-1):
//					{
//						try {
//							// get directories
//							String fxmlFileDir = constants.getEditDirectory();
//							String cssFileDir = constants.getCssDirectory();
//							// use FXMLLoader so the controller can be obtained later
//							FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileDir));
//							Parent root = loader.load();
//							// don't want to type much while initializing the controller
//							URL location = new URL(loader.getLocation().toString());
//
//							// controller setup
//							EditController controller = loader.getController();
//							controller.initData(new Recipe());
//							controller.initialize(location, loader.getResources());
//
//							// set scene
//							Scene editWindow = new Scene(root, MIN_SIZES[0], MIN_SIZES[1]);
//							editWindow.getStylesheets().add(getClass().getResource(cssFileDir).toExternalForm());
//							
//							// set stage
//							Stage originalStage = (Stage) motherPane.getScene().getWindow();
//							originalStage.setTitle("New Recipe - Edit Mode");
//							originalStage.setScene(editWindow);
//							originalStage.show();
//
//							// center the stage
//							Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
//							originalStage.setX((primScreenBounds.getWidth() - originalStage.getWidth()) / 2);
//							originalStage.setY((primScreenBounds.getHeight() - originalStage.getHeight()) / 2);
//
//						} catch (IOException ioe) {
//							AlertBox.display("Warning", "File not found.");
//						}
//						break;
//					}
//					case(0):
//						break;
//					case(1):
//					{
//						WriteData.CreateRecipeFile(recipeName.getText(), recipe);
//						try {
//							// get directories
//							String fxmlFileDir = constants.getEditDirectory();
//							String cssFileDir = constants.getCssDirectory();
//							// use FXMLLoader so the controller can be obtained later
//							FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileDir));
//							Parent root = loader.load();
//							// don't want to type much while initializing the controller
//							URL location = new URL(loader.getLocation().toString());
//
//							// controller setup
//							EditController controller = loader.getController();
//							controller.initData(new Recipe());
//							controller.initialize(location, loader.getResources());
//
//							// set scene
//							Scene editWindow = new Scene(root, MIN_SIZES[0], MIN_SIZES[1]);
//							editWindow.getStylesheets().add(getClass().getResource(cssFileDir).toExternalForm());
//							
//							//set stage
//							Stage originalStage = (Stage) motherPane.getScene().getWindow();
//							originalStage.setTitle("New Recipe - Edit Mode");
//							originalStage.setScene(editWindow);
//							originalStage.show();
//
//							// center the stage
//							Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
//							originalStage.setX((primScreenBounds.getWidth() - originalStage.getWidth()) / 2);
//							originalStage.setY((primScreenBounds.getHeight() - originalStage.getHeight()) / 2);
//
//						} catch (IOException ioe) {
//							AlertBox.display("Warning", "File not found.");
//						}
//						break;
//					}
//					}
//				} else {
//					try {
//						// get directories
//						String fxmlFileDir = constants.getEditDirectory();
//						String cssFileDir = constants.getCssDirectory();
//						// use FXMLLoader so the controller can be obtained later
//						FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileDir));
//						Parent root = loader.load();
//						URL location = new URL(loader.getLocation().toString());
//
//						// controller setup
//						EditController controller = loader.getController();
//						controller.initData(new Recipe());
//						controller.initialize(location, loader.getResources());
//
//						// scene setup
//						Scene editWindow = new Scene(root, MIN_SIZES[0], MIN_SIZES[1]);
//						editWindow.getStylesheets().add(getClass().getResource(cssFileDir).toExternalForm());
//						
//						// stage setup
//						Stage originalStage = (Stage) motherPane.getScene().getWindow();
//						originalStage.setTitle("New Recipe - Edit Mode");
//						originalStage.setScene(editWindow);
//						originalStage.show();
//
//						// center the stage
//						Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
//						originalStage.setX((primScreenBounds.getWidth() - originalStage.getWidth()) / 2);
//						originalStage.setY((primScreenBounds.getHeight() - originalStage.getHeight()) / 2);
//
//					} catch (IOException ioe) {
//						AlertBox.display("Warning", "File not found.");
//					}
//				}
				
				/**
				 * start a new recipe in edit mode
				 */
				try {
					// get directories
					String fxmlFileDir = constants.getEditDirectory();
					String cssFileDir = constants.getCssDirectory();
					// use FXMLLoader so the controller can be obtained later
					FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileDir));
					Parent root = loader.load();
					URL location = new URL(loader.getLocation().toString());

					// controller setup
					EditController controller = loader.getController();
					history.getBackward().push(constants.getEditDirectory());
					controller.initFlowingData(history, new Recipe(), readingData);
					/**
					 * if the user clicks backward button while making a new recipe
					 * it will bring them back to edit mode of that recipe
					 */
					controller.setRecipeFromRead(recipe);
					controller.initialize(location, loader.getResources());

					// scene setup
					Scene editWindow = new Scene(root, MIN_SIZES[0], MIN_SIZES[1]);
					editWindow.getStylesheets().add(getClass().getResource(cssFileDir).toExternalForm());
					
					// stage setup
					Stage originalStage = (Stage) motherPane.getScene().getWindow();
					originalStage.setTitle("New Recipe - Edit Mode");
					originalStage.setScene(editWindow);
					originalStage.show();

					// center the stage
					Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
					originalStage.setX((primScreenBounds.getWidth() - originalStage.getWidth()) / 2);
					originalStage.setY((primScreenBounds.getHeight() - originalStage.getHeight()) / 2);

				} catch (IOException ioe) {
					AlertBox.display("Warning", "File not found.");
				}
			}
		});

		// disable edit button
		menuEdit.setDisable(true);

		// ComboBox for ingredient's units
		ingreUnit.getItems().addAll(UNITS);
		// user can make their own units
		ingreUnit.setEditable(true);

		/**
		 * Sets up serving size ComboBox and
		 * its handler 
		 */
//		servingSize.setOnAction( e -> {
//			for (int i = 0; i < ingredients.size(); i++) {
//				double tempQty;
//				tempQty = Double.parseDouble(servingSize.getValue()) * qtyPerServingSize.get(i);
//				changeValueAt(i, 2, tempQty, ingredientsTable);
//			}
//		});

		/**
		 * menuSave
		 */
		menuSave.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				try {
					//check if name field is empty (because it's the file's name)
					if (recipeName.getText().equals(null) || recipeName.getText().equals("") || StringUtils.isBlank(recipeName.getText()))
						throw new IllegalArgumentException();
					boolean wantToSave = ConfirmBox.display("Notice", "Do you want make change(s) to this recipe?");
					if (wantToSave) {
						oldRep = recipe;
						WriteData.CreateRecipeFile(recipeName.getText(), recipe);
						AlertBox.display("Notice", recipeName.getText() + " is saved");
					}
					else return;
				} catch (IllegalArgumentException npe) {
					AlertBox.display("Warning", "Recipe name is empty");
				} 
			}

		});

		/**
		 * menuSaveAs handler
		 */
		menuSaveAs.setOnAction(action -> {
			menuSave.setDisable(false);
			try {
				// check if name field is empty (because it's the file's name)
				if (recipeName.getText().equals(null) || recipeName.getText().equals("") || StringUtils.isBlank(recipeName.getText()))
					throw new IllegalArgumentException();
				
				// check duplication
				RecipeList data = ReadData.readRecipes();
				if (data.getRecipeByName(recipeName.getText()).size() > 0) {
					boolean notChangeName = ConfirmBox.display("Warning", "You have not changed your recipe's name." 
							+ "\n" + "Clicking yes is to replace " + recipeName.getText());
					if (notChangeName) {
						oldRep = recipe;
						WriteData.CreateRecipeFile(recipeName.getText(), recipe);
						AlertBox.display("Notice", recipeName.getText() + " is saved");
					}
					else return;
				}
				// if recipe name is not duplicate, ask the user to confirm saving
				else {
					boolean wantToSave = ConfirmBox.display("Notice", "Do you want make change(s) to this recipe?");
					if (wantToSave) {
						//recipe.setInstructions(instructions.getText());
						oldRep = recipe;
						WriteData.CreateRecipeFile(recipeName.getText(), recipe);
						AlertBox.display("Notice", recipeName.getText() + " is saved");
					}
					else return;
				}
			} catch (IllegalArgumentException e) {
				AlertBox.display("Warnning", "Recipe name is empty");
			}
		});

		/**
		 * forward event handler
		 * Ex: Main -> list View -> read view -> edit view -> backward to read view -> forward to edit view
		 */
		forward.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					if (history.getForward().isEmpty()) return;
					else {
						// pop directory from top of forward stack
						String fxmlFileDir = history.getForward().pop();
						String cssFileDir = constants.getCssDirectory();
						// use FXMLLoader so the controller can be obtained later
						FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileDir));
						Parent root = loader.load();
						URL location = new URL(loader.getLocation().toString());
						Object controller = loader.getController();
						
						if (controller instanceof EditController) {
							history.getBackward().push(constants.getEditDirectory());
							((EditController) controller).initFlowingData(history, recipe, readingData);
							((EditController) controller).initialize(location, loader.getResources());
							
							Scene editView = new Scene(root, MIN_SIZES[0], MIN_SIZES[1]);
							editView.getStylesheets().add(getClass().getResource(cssFileDir).toExternalForm());
							
							Stage originalStage = (Stage) motherPane.getScene().getWindow();
							originalStage.setScene(editView);
							originalStage.setTitle(recipe.getName() + " - Edit Mode");
							originalStage.show();
							
							// center the stage
							Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
							originalStage.setX((primScreenBounds.getWidth() - originalStage.getWidth()) / 2);
							originalStage.setY((primScreenBounds.getHeight() - originalStage.getHeight()) / 2);
						}
						
					}
				} catch (Exception e) {
					AlertBox.display("Warning", "Oops! Something went wrong.");
				}
			}
		});
		
		/**
		 * backward event handler
		 * there are 3 possible cases for backward to handle 
		 * edit view -> edit view, edit view -> read view, edit view -> main view
		 */
		backward.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					// pop the top address from the backward list
					String fxmlFileDir = history.getBackward().pop();
					String cssFileDir = constants.getCssDirectory();
					FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileDir));
					Parent root = loader.load();
					URL location = new URL(loader.getLocation().toString());
					Object controller = loader.getController();
					
					// edit -> read
					if (controller instanceof ReadController) {
						history.getForward().push(constants.getEditDirectory());
						((ReadController) controller).initFlowingData(history, recipeFromRead, readingData);
						((ReadController) controller).setPrevRep(recipe);
						((ReadController) controller).initialize(location, loader.getResources());
						
						Scene readingView = new Scene(root, MIN_SIZES[0], MIN_SIZES[1]);
						readingView.getStylesheets().add(getClass().getResource(cssFileDir).toExternalForm());
						
						Stage originalStage = (Stage) motherPane.getScene().getWindow();
						originalStage.setScene(readingView);
						originalStage.setTitle(recipeFromRead.getName() + " - View Mode");
						originalStage.show();
						
						// center the stage
						Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
						originalStage.setX((primScreenBounds.getWidth() - originalStage.getWidth()) / 2);
						originalStage.setY((primScreenBounds.getHeight() - originalStage.getHeight()) / 2);
					}
					// edit -> main
					else if (controller instanceof WelcomeController) {
						Scene welcomeView = new Scene(root, MIN_SIZES[0], MIN_SIZES[1]);
						welcomeView.getStylesheets().add(getClass().getResource(cssFileDir).toExternalForm());
						
						Stage originalStage = (Stage) motherPane.getScene().getWindow();
						originalStage.setScene(welcomeView);
						originalStage.setTitle("Recipe Keeper");
						originalStage.show();
						
						// center the stage
						Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
						originalStage.setX((primScreenBounds.getWidth() - originalStage.getWidth()) / 2);
						originalStage.setY((primScreenBounds.getHeight() - originalStage.getHeight()) / 2);
					}
					// edit -> edit
					else if (controller instanceof EditController) {
						history.getForward().push(constants.getEditDirectory());
						((EditController) controller).initFlowingData(history, recipeFromRead, readingData);
						((EditController) controller).initialize(location, loader.getResources());
						
						Scene prevEditView = new Scene(root, MIN_SIZES[0], MIN_SIZES[1]);
						prevEditView.getStylesheets().add(getClass().getResource(cssFileDir).toExternalForm());
						
						Stage originalStage = (Stage) motherPane.getScene().getWindow();
						originalStage.setScene(prevEditView);
						originalStage.setTitle(recipeFromRead.getName() + " - View Mode");
						originalStage.show();
						
						// center the stage
						Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
						originalStage.setX((primScreenBounds.getWidth() - originalStage.getWidth()) / 2);
						originalStage.setY((primScreenBounds.getHeight() - originalStage.getHeight()) / 2);
					}
				} catch (Exception e) {
					AlertBox.display("Warning", "Oops! Something went wrong");
					e.printStackTrace();
				}
			}
		});

		/**
		 * return to home screen
		 */
		home.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (recipe.compareTo(oldRep) != 1) {
					int clickResult = ConfirmCancelBox.display("Notice", "Do you want to make changes to " + recipeName.getText() + "?"
							+ "\n" + "Your change(s) will be lost if you don't save them.");
					switch (clickResult) {
					case(-1):
						try {
							String fxmlFileDir = constants.getWelcomeDirectory();
							String cssFileDir = constants.getCssDirectory();
							Parent root = FXMLLoader.load(getClass().getResource(fxmlFileDir));
							Scene homeWindow = new Scene(root, MIN_SIZES[0], MIN_SIZES[1]);
							homeWindow.getStylesheets().add(getClass().getResource(cssFileDir).toExternalForm());
							Stage originalStage = (Stage) motherPane.getScene().getWindow();

							originalStage.setTitle("Recipe Keeper");
							originalStage.setScene(homeWindow);
							originalStage.show();

							// center the stage
							Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
							originalStage.setX((primScreenBounds.getWidth() - originalStage.getWidth()) / 2);
							originalStage.setY((primScreenBounds.getHeight() - originalStage.getHeight()) / 2);

						} catch (IOException ioe) {
							AlertBox.display("Warning", "Oops! Something wrong happened.");
						}
					break;
					case(0):
						break;
					case(1):
					{
						//recipe.setInstructions(instructions.getText());
						//oldRep = recipe;
						WriteData.CreateRecipeFile(recipeName.getText(), recipe);
						try {
							String fxmlFileDir = constants.getWelcomeDirectory();
							String cssFileDir = constants.getCssDirectory();
							Parent root = FXMLLoader.load(getClass().getResource(fxmlFileDir));
							Scene homeWindow = new Scene(root, MIN_SIZES[0], MIN_SIZES[1]);
							homeWindow.getStylesheets().add(getClass().getResource(cssFileDir).toExternalForm());
							Stage originalStage = (Stage) motherPane.getScene().getWindow();

							originalStage.setTitle("Recipe Keeper");
							originalStage.setScene(homeWindow);
							originalStage.show();

							// center the stage
							Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
							originalStage.setX((primScreenBounds.getWidth() - originalStage.getWidth()) / 2);
							originalStage.setY((primScreenBounds.getHeight() - originalStage.getHeight()) / 2);

						} catch (IOException ioe) {
							AlertBox.display("Warning", "Oops! Something wrong happened.");
						}
						break;
					}

					}
				}
				else {
					try {
						String fxmlFileDir = constants.getWelcomeDirectory();
						String cssFileDir = constants.getCssDirectory();
						Parent root = FXMLLoader.load(getClass().getResource(fxmlFileDir));
						Scene homeWindow = new Scene(root, MIN_SIZES[0], MIN_SIZES[1]);
						homeWindow.getStylesheets().add(getClass().getResource(cssFileDir).toExternalForm());
						Stage originalStage = (Stage) motherPane.getScene().getWindow();

						originalStage.setTitle("New Recipe - Edit Mode");
						originalStage.setScene(homeWindow);
						originalStage.show();

						// center the stage
						Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
						originalStage.setX((primScreenBounds.getWidth() - originalStage.getWidth()) / 2);
						originalStage.setY((primScreenBounds.getHeight() - originalStage.getHeight()) / 2);

					} catch (IOException ioe) {
						AlertBox.display("Warning", "Oops! Something wrong happened.");
					}
				}

			}
		});

		/**
		 * Sets up handler for unit ComboBox for ingredients 
		 * 
		 */
		ingreUnit.setOnAction(e -> {
			// add more code for listener if needed
		});

		/**
		 * EventHandler for adding an ingredient
		 */
		addIngredient.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {

					/**
					 * verify user's inputs, make sure they not empty, null or contains all blanks
					 */
					if (ingreName.getText().equals(null) || ingreQty.getText().equals(null) || ingreUnit.getValue().equals(null) || 
							ingreName.getText().equals("") || ingreQty.getText().equals("") || ingreUnit.getValue().equals("")) 
						throw new IngredientException("One or more fields are empty");
					//					else if (StringUtils.isBlank(ingreName.getText())) throw new IngredientException("Ingredient name cannot be blank");
					//					else if (StringUtils.isBlank(ingreQty.getText())) throw new IngredientException("Quantity cannot be blank");
					//					else if (StringUtils.isBlank(ingreUnit.getValue())) throw new IngredientException("Unit cannot be blank");
					else if (containsDigit(ingreName.getText())) throw new IngredientException("Ingredient name can't contains digit");
					else if (containsDigit(ingreUnit.getValue())) throw new IngredientException("Ingredient name can't contains digit");
					else if (!isNumeric(ingreQty.getText())) throw new IngredientException("Ingredient quantity");

					// add to displaying ingredient
					String name, unit;
					double qty;
					name = ingreName.getText();
					qty = Double.valueOf(ingreQty.getText());
					unit = ingreUnit.getValue();
					Ingredient i = new Ingredient (name, qty, unit);
					// add to view and internal data
					ingredients.add(i);
					//qtyPerServingSize.add(qty);
					recipe.getIngredients().add(i);
				} 
				catch (IngredientException e) { 
					// add more codes if needed
				}
				catch (IllegalArgumentException np) {
					AlertBox.display("Warning", "Cannot read input fields");
				}
				catch (NullPointerException e) {
					AlertBox.display("Warning", "One or more fields are empty");
				}
				catch (Exception ex) {
					//ex.printStackTrace();
				}
			}
		});

		/**
		 * EventHandler for deleting an ingredient
		 */
		delIngredient.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					if (ingredients.size() < 1) throw new RuntimeException(); 
					int selectedIndex = ingredientsTable.getSelectionModel().getSelectedIndex();
					if (selectedIndex >= 0) {
						// remove from view and internal data
						ingredientsTable.getItems().remove(selectedIndex);
						//qtyPerServingSize.remove(selectedIndex);
						recipe.getIngredients().remove(selectedIndex);
					}
					else {
						AlertBox.display("Warning", "No Item Selected");
					}
				} catch (RuntimeException e) {
					AlertBox.display("Warning", "Ingredient List Is Empty");
				}
			}
		});

		/**
		 * Event Handler for adding a category
		 */
		addCategory.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					if (textCategory.getText().equals(null) || textCategory.getText().equals("")) throw new NullPointerException();
					else if (textCategory.getText().contains("[a-zA-Z]+")) throw new IllegalArgumentException();
					else if (StringUtils.isBlank(textCategory.getText())) throw new IngredientException();
					// add to view and internal data
					categories.add(textCategory.getText());
					recipe.getCategories().add(textCategory.getText());
				} catch (NullPointerException npe) {
					AlertBox.display("Warning", "Category field cannot be empty");
				} catch (IllegalArgumentException e) {
					AlertBox.display("Warning", "Category field cannot be a number");
				} catch (IngredientException ie) {
					AlertBox.display("Warning", "Category field cannot contain all spaces");
				}
			}
		});

		/**
		 * Event Handler for deleting a category
		 */
		delCategory.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					if (categories.size() < 1) throw new RuntimeException();
					int selectedIndex = categoryTable.getSelectionModel().getSelectedIndex();
					if (selectedIndex >= 0) {
						// remove from view and internal data
						categoryTable.getItems().remove(selectedIndex);
						recipe.getCategories().remove(selectedIndex);
					}
					else {
						AlertBox.display("Warning", "No Item Selected");
					}
				} catch (RuntimeException e) {
					AlertBox.display("Warning", "Category List Is Empty");
				}
			}
		});
	}

	/**
	 * Initialize data, Recipe setter
	 * @param recipe
	 */
	public void initData(Recipe r) {

		this.recipe = r;
		//this.oldName = r.getName();
		this.oldRep = r;
		recipeName.setText(recipe.getName());
		instructions.setText(recipe.getInstructions());

//		// servingSize
//		servingSize.getItems().addAll(SERVSIZES);
//		servingSize.setStyle("-fx-background-color: #ff9900");

		// Ingredient column
		TableColumn<Ingredient, String> ingredientColumn = new TableColumn<>("Ingredient");
		ingredientColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

		// Quantity column
		TableColumn<Ingredient, String> quantityColumn = new TableColumn<>("Quantity");
		quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));	

		// Quantity column
		TableColumn<Ingredient, String> unitColumn = new TableColumn<>("Unit");
		unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

		// Set ingredientsTable
		for (Ingredient i : recipe.getIngredients()) {
			ingredients.add(i);
			//qtyPerServingSize.add(i.getQuantity());
		}
		ingredientsTable.setItems(ingredients);
		ingredientsTable.getColumns().addAll(ingredientColumn, quantityColumn, unitColumn);

		// Initialize Category table
		for (String s : recipe.getCategories()) {
			categories.add(s);
		}
		categoryTable.setItems(categories);

		// disable Save button if user enter edit mode
		if (r.getName().equals("")) {
			menuSave.setDisable(true);
		}
	}
	
	/**
	 * initialize and store flowing data between scenes
	 * @param history
	 * @param r
	 * @param repList
	 */
	public void initFlowingData(Addresses history, Recipe r, ArrayList<Recipe> repList) {
		initData(r);
		setRecipeFromRead(r);
		setHistory(history);
		setReadingData(repList);
	}

	/**
	 * get Data, recipe getter
	 * @return the recipe is being read
	 */
	public Recipe getData() {
		return this.recipe;
	}

	/**
	 * Check if a string is numeric
	 * @param str
	 * @return true if it is, false if otherwise
	 */
	private static boolean isNumeric(String str)  
	{  
		try  
		{  
			Double.parseDouble(str);  
		}  
		catch(NumberFormatException nfe)  
		{  
			return false;  
		}  
		return true;  
	}

	/**
	 * Change value at specific location of TableView
	 * used for servingSize onAction
	 * @param row
	 * @param col
	 * @param value
	 * @param table
	 */
//	private static void changeValueAt(int row, int col, double value, TableView<Ingredient> table) {
//		Ingredient newData = table.getItems().get(row);
//		newData.setQuantity(value);
//		table.getItems().set(row, newData);
//	}	

	/**
	 * check if a String contains a digit
	 * @param a String
	 * @return true if it does, false otherwise
	 */
	private static boolean containsDigit(String s) {
		boolean result = false;
		for (char c : s.toCharArray()) {
			if (Character.isDigit(c)) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * @return accessing history
	 */
	public Addresses getHistory() {
		return history;
	}
	/**
	 * set accessing history
	 * @param history
	 */
	public void setHistory(Addresses history) {
		this.history = history;
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList<Recipe> getReadingData() {
		return readingData;
	}

	/**
	 * 
	 * @param readingData
	 */
	public void setReadingData(ArrayList<Recipe> readingData) {
		this.readingData = readingData;
	}

	/**
	 * 
	 * @return
	 */
	public Recipe getRecipeFromRead() {
		return recipeFromRead;
	}

	/**
	 * 
	 * @param recipeFromRead
	 */
	public void setRecipeFromRead(Recipe recipeFromRead) {
		this.recipeFromRead = recipeFromRead;
	}

}
