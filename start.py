#!/usr/bin/python

from ATHPy import *  # @UnusedWildImport # https://github.com/aarontharris/ATHPy

SERVICE_NAME = "DataBuddy"
DEFAULT_PORT = 25566
JAR_PATTERN = './target/DataBuddy*.jar'

opts = GetOpts()
def main():  # {
    opts.addDescription( "Start %s with a specific port" % SERVICE_NAME )
    opts.addDescription( "You may see an error if the port is unavailable" )
    opts.addDescription( "The default port=%s" % DEFAULT_PORT )
    opts.add( "port", "p", "int", False, "The port to run %s on" % SERVICE_NAME )
    opts.addHelp()

    if opts.buildSafe( sys.argv ):  # {
        port = opts.get( "port", DEFAULT_PORT )
        jarFile = EnvUtl.execute( "ls %s" % JAR_PATTERN )
        os.system( "java -jar -D%s %s &" % ( port, jarFile ) )
    # }
# }

if __name__ == "__main__": main()
