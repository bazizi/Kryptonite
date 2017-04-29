# Kryptonite
Kryptonite is a file encryption utility that I made in Java

## Usage
- View help:
`java -jar Kryptonite.jar -h`



- Encrypt directory:

     `java -jar Kryptonite.jar -ed /PATH/TO/DIR /PATH/TO/KEY`

- Encrypt file:

     `java -jar Kryptonite.jar -ef /PATH/TO/FILE /PATH/TO/KEY`

- Decrypt directory:

     `java -jar Kryptonite.jar -dd /PATH/TO/DIR /PATH/TO/KEY`

- Decrypt file:

     `java -jar Kryptonite.jar -df /PATH/TO/FILE /PATH/TO/KEY`


The key could be any file. To have the most entropy I usually generate a key using the following command in Linux:

`cat /etc/urandom`

## GUI
Running `java -jar Kryptonite.jar` brings up the GUI, however, I no longer maintain the GUI and I do not recommend the GUI being used.

## Technical Specs
- Kryptonite uses bit-wise Vigenere cipher to encrypt files
- Kryptonite uses SHA-256 to validate the integrity of encrypted/decrypted files.
- Kryptonite prevents a file from being encrypted multiple times (To avoid corrupting files by mistake)
- Kryptonite prevents an unencrypted file to be decrypted or an encrypted file to be decrypted twice


## Disclaimer
I will not be responsible for wrong usage of Kryptonite (e.g. as randomsare etc.).
