Copyright (C) 2014 Hiroshi Manabe
Copyright (C) 2012 Nguyen Viet Cuong, Ye Nan, Sumit Bhagwani

This is the README file for HOFastCRF version 1.0.
HOFastCRF is based on HOSemiCRF by Nguyen Viet Cuong, Ye Nan, Sumit Bhagwani.

HOFastCRF is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

HOFastCRF is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with HOFastCRF. If not, see <http://www.gnu.org/licenses/>.

=== WARNING ===

HOFastCRF requires a lot of memory usage. It is best to run the program 
in parallel on a computing cluster with lots of memory.

=== COMPILATION STEPS ===

Requirement: Apache Ant (http://ant.apache.org/)

1. Download the HOFastCRF repository as a zip file: HOFastCRF-master.zip
2. Unzip the file:

    unzip HOFastCRF-master.zip

3. Compile the program:

    cd HOFastCRF-master
    
    ant

=== RUN THE OCR PROGRAM ===

Download data from http://www.seas.upenn.edu/~taskar/ocr/ to the folder run/ocr/
    
    cp dist/lib/HOFastCRF.jar run/ocr/
    cd run/ocr
    java -cp "HOFastCRF.jar" OCR.OCR all ocr.conf 0
    
=== MORE INFO ===

Please visit: https://github.com/hiroshi-manabe/HOFastCRF/wiki
