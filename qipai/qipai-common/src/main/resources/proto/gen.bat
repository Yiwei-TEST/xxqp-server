echo on
set output_path=..\..\java
for %%i in (.\files\*.proto) do (
protoc-2.5.0-windows-x86_64.exe --java_out=%output_path% %%i
)
pause