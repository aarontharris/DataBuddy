#!/usr/bin/python

from ATHPy import *

def main():  # {
    pathToFile = EnvUtl.execute( "ls target/DataBuddy*.jar" )  # FIXME: unix dependency
    mvnDesc = MavenDescriptor( 'com.leap12.databuddy', 'DataBuddy', '0.0.1-SNAPSHOT', 'jar' )
    mvnDesc.installFile( pathToFile )
# }

if __name__ == "__main__": main()
