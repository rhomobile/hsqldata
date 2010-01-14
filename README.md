* Build jarfile using ant:
  * ant hsql

* This produces:
  * bin/hsqldata.jar
   
* To execute hsqldata jar, run:
  * <code>java -cp bin/hsqldata.jar com.rhomobile.hsqldata.HsqlData sqlitedatafile hsqldbfile schemafile schemaindexfile</code>
  
* For example:
  * <code>java -cp bin/hsqldata.jar com.rhomobile.hsqldata.HsqlData 1.data 1.hsqldb syncdb.schema syncdb.index.schema</code>