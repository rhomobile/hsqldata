* Build jarfile using ant:
  * ant hsql

* This produces:
  * bin/hsqldata.jar
   
* To execute hsqldata jar, run:
  * <code>java -cp bin/hsqldata.jar com.rhomobile.hsqldata.HsqlData sqlitedatafile hsqldbfile schemafile</code>
  
* For example:
  * <code>java -cp bin/hsqldata.jar com.rhomobile.hsqldata.HsqlData syncdb.sqlite syncdb syncdb.schema</code>