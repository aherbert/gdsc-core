The GDSC Core code was developed using the Eclipse IDE:
https://eclipse.org/

The code can be built using Maven. See the README.md for details. However using 
Eclipse is preferred for code development to provide a debugging environment.

You will need the Maven and Git Eclipse plugins. The standard Eclipse IDE for
Java developers has these.

Set-up the project
------------------

Import the project into Eclipse (File>Import)
Select: Maven > Existing Maven projects

This will import the project but may not link it to the source Git repository.
Right-click on the project name and select 'Team > Share'. If you share it back to 
the same location it will attach to the source Git repository.

Code formatting
---------------

The Eclipse code format rules (in this directory) can be loaded using:

Eclipse Preferences : Java > Code Style > Formatter

Click 'Import...' to load the provided rules.

Running the code
----------------

This code is a library to be used by other projects. There is no need to run the
code from Eclipse.
