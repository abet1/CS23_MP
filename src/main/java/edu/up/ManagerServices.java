package edu.up;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ManagerServices {
    public static void addItem(Scanner userInput){
        System.out.println("\nAdd Item");

        String name;
        do {
            System.out.print("Enter Name: ");
            name = userInput.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Error: Name cannot be empty. Please enter a valid name.");
            }
        } while (name.isEmpty());

        String itemType;
        do {
            System.out.print("Enter Item Type (Drink, Food, Merchandise): ");
            itemType = userInput.nextLine().trim();
            if (!(itemType.equalsIgnoreCase("Drink") || itemType.equalsIgnoreCase("Food") || itemType.equalsIgnoreCase("Merchandise"))) {
                System.out.println("Invalid input. Please only enter Drink, Food, or Merchandise.");
            }
        } while (!(itemType.equalsIgnoreCase("Drink") || itemType.equalsIgnoreCase("Food") || itemType.equalsIgnoreCase("Merchandise")));

        itemType = editName(itemType);

        String category;
        do {
            System.out.print("Enter Category: ");
            category = userInput.nextLine().trim();
            if (category.isEmpty()) {
                System.out.println("Error: Category cannot be empty. Please enter a valid category.");
            } else if (category.equalsIgnoreCase(itemType)) {
                System.out.println("Please add a category that is different from item type.");
            }
        } while (category.isEmpty() || category.equalsIgnoreCase(itemType));
        category = editName(category);

        String itemCode = itemCodeGenerator(category, name);

        String sizePrice = checkValidityOfSize(userInput);

        String customization = "None";
        if (!itemType.equalsIgnoreCase("Merchandise")) {
            customization = checkValidityOfCustom(userInput, customization);
        }
            // This ensures that if non merchandise items with "None" is inputted as such
        if(customization.equalsIgnoreCase("None")) {
            customization = editName(customization);
        }



        SQLDriver.sqlAddMenuItem(itemCode, name, itemType ,category, sizePrice, customization);
    }

    public static void encodeItem(Scanner userInput){
        System.out.println("\nEncode Item");
        System.out.print("Enter the filename to encode items(txt file): ");
        String filename = userInput.nextLine();
        List<Item> items = SQLDriver.sqlLoadMenuItems();

        try{
            File file = new File(filename);
            Scanner fileReader = new Scanner(file);


            while(fileReader.hasNextLine()){
                String line = fileReader.nextLine();
                String [] itemInformation = line.split(",");
                if(itemInformation.length == 6){
                    String itemCode = itemInformation[0];
                    String itemName = itemInformation[1];
                    String itemType = itemInformation[2];
                    String itemCategory = itemInformation[3];
                    String itemSize = itemInformation[4];
                    String customizations = itemInformation[5];

                    if (itemCode == null || itemCode.isBlank() || isDuplicateItemCode(items, itemCode)){
                        itemCode = itemCodeGenerator(itemCategory, itemName);
                    }

                    SQLDriver.sqlAddMenuItem(itemCode, itemName, itemType, itemCategory, itemSize, customizations);
                    System.out.println("Item " + itemCode +":"+itemName+ " added to the database");
                }else{
                    System.out.println("Invalid format in file line: " + line);
                }
            }

        }catch(FileNotFoundException e){
            System.out.println("File not found" + e.getMessage());
        }
    }

    private static boolean isDuplicateItemCode(List<Item> items, String itemCode){
        for(Item item : items){
            if(itemCode.equalsIgnoreCase(item.getItemCode())){
                return true;
            }else{
                return false;
            }
        }
        return false;
    }

    public static void modifyItem(Scanner userInput){
        Item itemToModify = null;

        System.out.println("\nModify Item");
        System.out.print("Enter the item code of the item you want to modify: ");
        String itemCode = userInput.nextLine().trim();
        itemToModify = SQLDriver.sqlFindMenuItemByItemCode(itemCode);

        if(itemToModify == null){
            System.out.println("Error: Item not found. Please enter a valid item code.");
            return;
        }
        System.out.println("\nCurrent details of item " + itemToModify.getItemCode());
        System.out.println("Name: " + itemToModify.getName());
        System.out.println("Category: " + itemToModify.getCategory());
        System.out.println("Size and Price: " + itemToModify.getSizePrice());
        System.out.println("Customization: " + itemToModify.getCustomization());// will output None for merchandise items
        String choice = "0";

        while(!choice.equals("3")) {
            System.out.println("\nWhat do you want to modify?");
            System.out.println("1. Size and Price");
            // does not output if the item is classified as merchandise
            if (!itemToModify.getItemType().equalsIgnoreCase("Merchandise")) {
                System.out.println("2. Customization");
            }
            System.out.println("3. Exit");
            choice = userInput.nextLine().trim();
            switch (choice) {
                    case "1":
                        String newSizePrice = checkValidityOfSize(userInput); // validates if new size price is in the correct format
                        itemToModify.setSizePrice(newSizePrice);
                        break;
                    case "2":
                        String newCustomization = "None";
                        if (!itemToModify.getItemType().equalsIgnoreCase("Merchandise")) {
                            newCustomization = checkValidityOfCustom(userInput, newCustomization); // validates if customization is in correct format
                            itemToModify.setCustomization(newCustomization);
                        } else { // if user accidentally presses 2 in the menu
                            System.out.println("Cannot modify customization for merchandise items.\n");
                        }
                        break;
                    case "3":
                        System.out.print("Exiting modify item menu. Goodbye!\n");
                        break;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        String sizePrice=itemToModify.getSizePrice();
        String customizations=itemToModify.getCustomization();

        SQLDriver.sqlModifyMenuItem(itemCode, sizePrice, customizations);


    }

    public static void deleteItem(Scanner userInput){
        Item itemToDelete = null;
        System.out.println("\nDelete Item");
        System.out.print("Enter the item code of the item you want to delete: ");
        String itemCode = userInput.nextLine().trim();

        itemToDelete = SQLDriver.sqlFindMenuItemByItemCode(itemCode);

        if (itemToDelete == null) {
            System.out.println("Item with code" + itemCode + " does not exist in the menu.");
            return;
        }

        System.out.println("\n Current details of item " + itemToDelete);
        System.out.println("Name: " + itemToDelete.getName());
        System.out.println("Category: " + itemToDelete.getCategory());
        System.out.println("Size and Price: " + itemToDelete.getSizePrice());
        System.out.println("Customization: " + itemToDelete.getCustomization());

        System.out.println("Are you sure you want to delete this item? (yes/no)");
        String choice = userInput.nextLine().trim().toLowerCase();
        switch (choice) {
            case "yes":
                SQLDriver.sqlDeleteMenuItem(itemCode);
                break;
            case "no":
                System.out.println("Item with code" + itemCode + " will not be deleted. Goodbye!");
                return;
            default:
                System.out.println("Invalid choice. Try again.");
        }
    }

    public static String itemCodeGenerator(String category, String name){
        String categoryCode;
        if (category.length() < 2) {
            categoryCode = category.substring(0, 1).toUpperCase() + "0";
        } else {
            categoryCode = category.substring(0, 1).toUpperCase() + category.substring(category.length() - 1).toUpperCase();
        }

        // ensures name code is exactly 4 characters, replaces with '0' if necessary
        String nameCode = name.toUpperCase();
        if (nameCode.length() < 4) {
            nameCode = String.format("%-4s", nameCode).replace(' ', '0'); // Pad with '0'
        } else {
            nameCode = nameCode.substring(0, 4);
        }

        List<Item> categoryitems = SQLDriver.sqlFindMenuItemsByCategory(category);
        List<Integer> usedCodeNumbers = new ArrayList<>();

        for(Item item : categoryitems){
            String [] parts = item.getItemCode().split("-");
            if(parts.length == 3){
                try{
                    usedCodeNumbers.add(Integer.parseInt(parts[2]));
                }catch(NumberFormatException e){
                    System.out.println("Invalid item code: " + item.getItemCode());
                }
            }
        }

        int availableNumber = 1;
        while(usedCodeNumbers.contains(availableNumber)){
            availableNumber++;
        }
        String itemNumber = String.format("%03d", availableNumber);
        return categoryCode +"-"+nameCode +"-"+ itemNumber;
    }

    public static String editName(String originalName) {
        return originalName.substring(0, 1).toUpperCase() + originalName.substring(1).toLowerCase();
    }

    public static boolean isValidInput(String input, boolean isCustomization) {
        // checks if input is empty
        if (input.isEmpty()) {
            return false; // Return false if input is empty
        }
        // defines the regex for a single "key=value" pair (e.g., Small=99 or Soy Milk=45)
        String pairRegex = "[a-zA-Z\\s]+=[0-9]+";
        // defines the regex for multiple "key=value" pairs separated by commas
        String multiplePairsRegex = pairRegex + "(,\\s*" + pairRegex + ")*";
        // if it's a customization, allow 'None' or valid key-value pairs
        if (isCustomization) {
            if (input.equalsIgnoreCase("None")) {
                return true; // 'None' is allowed for customization
            }
            // otherwise, validates using the multiplePairsRegex
            return input.matches(multiplePairsRegex);
        }
        // for sizes and prices, validates using the same multiplePairsRegex
        return input.matches(multiplePairsRegex);
    }

    public static String checkValidityOfSize(Scanner userInput) {
        String sizePrice;
        do {
            System.out.print("Enter Sizes and their corresponding prices (ex. Small=99, Medium=149, Large=199):  ");
            sizePrice = userInput.nextLine().trim();
            if (sizePrice.isEmpty()) {
                System.out.println("Error: Size and price cannot be empty. Please enter valid sizes and prices.");
            } else if (!isValidInput(sizePrice, false)) {
                System.out.println("Invalid format for sizes and prices. Please use the correct format (e.g., Small=99).");
            }
        } while (sizePrice.isEmpty() || !isValidInput(sizePrice, false));
        return sizePrice;
    }

    public static String checkValidityOfCustom(Scanner userInput, String customization) {
        do {
            System.out.println("Enter customization and their corresponding prices (ex. Soy Milk=45, Oat Milk=40)");
            System.out.print("Put 'None' if you have no customization: ");
            customization = userInput.nextLine().trim();
            if (customization.isEmpty()) {
                System.out.println("Error: Customization cannot be empty. Please enter a valid customization or 'None'.");
            } else if (!isValidInput(customization, true)) {
                System.out.println("Invalid customization format. Please use the correct format (e.g., Soy Milk=45) or 'None' for no customization.");
            }
        } while (customization.isEmpty() || !isValidInput(customization, true));
        return customization;
    }


}
