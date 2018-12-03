GDSC ImageJ Core
================

This package contains core code used by the GDSC ImageJ and GDSC SMLM ImageJ 
plugins. It is a dependency for both of those packages.


[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Build Status](https://travis-ci.com/aherbert/gdsc-core.svg?branch=master)](https://travis-ci.com/aherbert/gdsc-core)
[![Coverage Status](https://coveralls.io/repos/github/aherbert/gdsc-core/badge.svg?branch=master)](https://coveralls.io/github/aherbert/gdsc-core?branch=master)


Maven Installation
------------------

This package is used by other GDSC packages. It is only necessary to perform an 
install if you are building the other packages from the source code.

The code depends on the gdsc-analytics and gdsc-test artifacts so you will have 
to install this to your local Maven repository before building:

1. Clone the required repositories

        git clone https://github.com/aherbert/gdsc-analytics.git
        git clone https://github.com/aherbert/gdsc-test.git
        git clone https://github.com/aherbert/gdsc-core.git

2. Build the code and install using Maven

        cd gdsc-analytics
        mvn install
        cd ..
        cd gdsc-test
        mvn install
        cd ..
        cd gdsc-core
        mvn install
        cd ..

	This will produce gdsc-[artifact]-[VERSION].jar files in the local Maven
	repository. You can now build the other GDSC packages that depend on this
	code.


Legal
-----

See [LICENSE](LICENSE.txt)


# About #

###### Owner(s) ######
Alex Herbert

###### Institution ######
[Genome Damage and Stability Centre, University of Sussex](http://www.sussex.ac.uk/gdsc/)

###### URL ######
http://www.sussex.ac.uk/gdsc/intranet/microscopy/UserSupport/AnalysisProtocol/imagej/gdsc_plugins/
