path C:\Miniconda-x64;C:\Miniconda-x64\Scripts;%PATH%
conda env create -n lenskit etc\environment.yml
activate lenskit
gradlew.bat ciPrep
