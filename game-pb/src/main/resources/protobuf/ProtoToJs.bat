for %%i in (.\proto\*.proto) do (

      protoc --proto_path=./proto/ --js_out=import_style=commonjs,binary:js %%i
 
)
pause