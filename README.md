# Worth
Università di Pisa
Department of Computer Science.

WORTH is a university project for Network lab course that consists of the creation of a tool to organize and to manage projects in a group setting.

COMPILE
```js
javac --release 8 -d "bin" -cp ".\lib\jackson-core-2.9.7.jar;.\lib\jackson-annotations-2.9.7.jar;.\lib\jackson-databind-2.9.7.jar" .\src\*.java .\src\Support\*.java .\src\Rmi\*.java .\src\Interfaces\*.java
```
RUN

Server: 
```js 
java -cp ".\lib\jackson-core-2.9.7.jar;.\lib\jackson-annotations-2.9.7.jar;.\lib\jackson-databind-2.9.7.jar;bin" src.MainServer 
```


Client: 
```js 
java -cp ".\lib\jackson-core-2.9.7.jar;.\lib\jackson-annotations-2.9.7.jar;.\lib\jackson-databind-2.9.7.jar;bin" src.MainClient 
```

