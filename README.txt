=================================
GENERAL
=================================
All files within the directory are vital

=================================
PROOF SYSTEMS
=================================


=================================
OPERATING SYSTEM
=================================
Maude works 'out of the box' with Linux and MacOS.
Maude works within the Windows Subsystem for Linux.
-------
Linux:
-------
- maude.linux64

-------
Mac:
-------
- maude.darwin64

-------
Windows:
-------
With the introduction of the Windows Subsystem for Linux
this software can be run on Windows. In order to do so,
once must install and enable the Windows Subsystem.

 - https://docs.microsoft.com/en-us/windows/wsl/install-win10

=================================
Troubleshooting
=================================
This information is for people who are trying to figure
out exactly how the code works. Some of the code is
effectively legacy code (2006), but is still standard
and works normally. This is in particular true for 
the parsing.

-------
Editing Maude.java - for future troubleshooting
-------
In the file Maude.java at line 33,

 - maudecmd[0] = "./maude.linux64"

must be changed to the name of the Maude file,
therefore to switch between Mac and Linux it must
be done here.

In order to use the software on Windows, then because
bash commands can be run from a windows cmd, (if the 
subsystem is installed), line 46 must be changed.

 - ProcessBuilder pb = new ProcessBuilder(maudecmd)

must be replaced by

 - String cmd = "\"".concat(String.join(" ",maudecmd)).concat("\"");
 - ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "bash -c ".concat(cmd));











