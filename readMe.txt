How to use this classification jar?
1) You need to have your input data in the format of , separated attributes with the last column as the label of the sample.
2) Save the data in step 1 with a filename - filename.txt and place it in the src/main/resources/input folder
3) Edit the file project.properties in src/main/resources - you need to make 3 changes here 
   a) The name of the input file should match filename.txt selected in step 2.
   b) Modify the number of attributes to reflect the content of the file - ( class id i.e. the last column is not counted towards numberOfAttributes )
   c) Ensure the delimiter is correct. You can choose to edit the number of clusters if you like. 
4) navigate yourself to the parent folder where the repository is downloaded and run ./deployment/deploy.sh - the program output will be displayed on the terminal window.
