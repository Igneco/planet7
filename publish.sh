#!/bin/bash

echo "."
read -p "UPDATE THE VERSION NUMBER before you hit ENTER"
echo "."

./sbt "+ test" && ./sbt "+ publishSigned" && ./sbt sonatypeReleaseAll
