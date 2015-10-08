#! /bin/sh

# This script fetches test data for DerStandardParserTest.java
# Copyright and License for this script: same as DerStandardParserTest.java, see there.  

wget -O 7613-puzzle "http://derstandard.at/raetselapp/?id=7613&cd=&url=http%3a%2f%2fderstandard.at%2f1388650404174"
wget -O 7613-solution --post-data=ExternalId=7613 http://derstandard.at/RaetselApp/Home/GetCrosswordResult