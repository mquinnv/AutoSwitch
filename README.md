# AutoSwitch #

AutoSwitch is a very advanced mod that automatically switches your tools and weapons.

See [http://is.gd/ThebombzensMods#AutoSwitch](http://is.gd/ThebombzensMods#AutoSwitch) for details.

Note: If you want to contribute, feel free! Just send me a pull request and I'll review it before adding it.

## Compiling ##

First, you need to clone [ThebombzenAPI](https://github.com/thebombzen/ThebombzenAPI) to the same directory that you cloned AutoSwitch. i.e. you should see ThebombzenAPI/ and AutoSwitch/ in the same directory.

Then navigate to AutoSwitch and run:

	$ ./build.sh

This will create the directory "build" which and all build supplies inside of it, and should create a finished AutoSwitch jar file upon completion.

On Windows? Sorry, you're on your own. I don't know how to write CMD Batch files. 

## Eclipse ##

Once you've run the buildscript at least once, you can go to Eclipse and select File -> Import -> Existing Projects to Workspace, and select AutoSwitch as the root project directory. If you have the Git plugin it should recognize AutoSwitch as a git respository.

## Releases ##

The releases in the upper-right contain intermediary releases that don't bump the version number. This is to publish hotfixes without reminding everyone to update.


