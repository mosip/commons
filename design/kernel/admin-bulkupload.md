# Bulk upload create and update

#### Background

The master data should be allowed to create or update the data in bulk. The user uploads a CSV file with the data. The first row represents the column names and the rest of the rows are the data which will be inserted into the table. The user selects the table from a drop down and selects the operation as Insert or Update or Upsert. Once the file is uploaded, the result messages are displayed in the front end. 


#### Solution


**The key solution considerations are**

- Following are the key considerations during the creation of master data, 

	- The user uploads the CSV file with the data, which have to be inserted. The valid file format is CSV. 
	
	- The uploaded file has the column names and their data. The first row is reserved for the columns and and remaing rows are for the data. 
	
	- There are 2 types of functional parameters, which is set in the front end. 
		1. When the user selects the table name from the drop down, "TABLE" parameter is set. 
		2. When the user selects the type of actions, "Insert" or "Update" or "Upsert", the "FUNCTION" action is set in the front end. 

	- The validator utility validates the following, 
		1. The file format of the uploaded file
		2. The structure of the content is correct. 
			a. the first row is for the table column names
			b. all the rows should have the equal number of column size
		3. The data type and the length validations are NOT validated in the code. This functionality is already available in the RDBMS
		4. The allowed date format should be ISO-8601. TODO: Date format have to be mentioned. The date is converted into UTC before saving into the database. 
	
	- The table names are created in the Config server along with the primary keys and the level. The UI dropdown renders the table names from the Config server. The master_data configurations are stored as a JSON configuraion file. For example, [{"table_name":"", "primary_keys":[], "level":1}]
	
	- The levels starts from 1 to n. If the level is 1, these table date have to be inserted before the level 2. 
	
	- The entire master_data configurations have to be cached on the first read and populated as the Java representation, for faster retrieval. The primary keys are used during the update functionaity. 
	
	- The Insert operation uses a generic utility, which accepts the Table name. The column names corresponding to the Table name is fetched from the first row of the CSV file. The INSERT query is constructed for all the rows in the CSV records. The entire list of queries are executed and any exceptions are added to the ExceptionList. Example query for Insert is, "insert into tbl_name(col_1,col_2,col_3,col_4) values (?,?,?,?")
	
	- The Update operation uses a generic utility. This accepts the table name. The primary keys are identified by they master_data configuration file. This primary keys are used for update condition. The condition for the update operation is based only the primary keys. 
	
	- In case of exceptions, the entire insertion activities are rolled back. The ExceptionList is given back to the UI, so that the errors are populated in the front end. 

	- The disadvantage of this method is, the system uses the native query. 
	
	- The advantage is, if we want to add any new Table, we don't need to create the JPA Entity classes. 
	