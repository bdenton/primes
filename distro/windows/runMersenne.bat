@echo off
set LD_LIBRARY_PATH=.
java -Djava.library.path=%LD_LIBRARY_PATH% -cp . -jar mersenne3.jar %* ./primes
