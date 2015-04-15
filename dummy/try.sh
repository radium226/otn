cd ..
mvn clean install
cd - 
rm -Rf ~/.m2/repository/com/oracle/
mvn clean package
