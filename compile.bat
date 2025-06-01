@echo off
echo Compiling Banking Management System...

if not exist target\classes mkdir target\classes

echo Compiling configuration classes...
javac -cp . -d target\classes src\main\java\com\bankingsystem\config\*.java

echo Compiling exception classes...
javac -cp target\classes -d target\classes src\main\java\com\bankingsystem\exceptions\*.java

echo Compiling model classes...
javac -cp target\classes -d target\classes src\main\java\com\bankingsystem\models\*.java

echo Compiling utility classes...
javac -cp target\classes -d target\classes src\main\java\com\bankingsystem\utils\*.java

echo Compiling DAO classes...
javac -cp target\classes -d target\classes src\main\java\com\bankingsystem\dao\*.java

echo Compiling service classes...
javac -cp target\classes -d target\classes src\main\java\com\bankingsystem\services\*.java

echo Compiling GUI classes...
javac -cp target\classes -d target\classes src\main\java\com\bankingsystem\gui\*.java

echo Compiling main application...
javac -cp target\classes -d target\classes src\main\java\com\bankingsystem\main\*.java

echo Compilation complete!
echo Run the application using: run-banking-system.bat
pause
