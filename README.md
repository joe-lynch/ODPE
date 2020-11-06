# ODPE
The Open Deduction Proof Editor allows proof theorists (or anyone) to deconstruction proofs step by step. It currently supports deep inference formalisms, namely Open Deduction and naturally the Calculus of Structures. The front end of the application is written in Java, while the back end is written in Maude. There is also an implementation of proof search both with and without proof variables.
Github: https://github.com/joe-lynch/ODPE

======================================================================
GUIDE
======================================================================
-------------
MAUDE
-------------
You must have Maude 3.0 (or above) installed on your system.
You can download it at the link below, see the section
OPERATING SYSTEM, especially if you are on Windows.

- http://maude.cs.illinois.edu/w/index.php/The_Maude_System

Please name the file to 'maude.linux64' for Linux 
Please name the file to 'maude.darwin64' for MacOS
[See OPERATING SYSTEM]

Ensure that you have made the file executable, for Linux you can do
- chmod +x maude.linux64

For some Linux systems (Arch, lubuntu, etc), ensure you have libinfo5
installed.
- sudo apt install libinfo5

You can test if Maude is working by running
- ./maude.linux64

-------------
JAVA
-------------
Java must also be on your system - the odpe.jar file is compiled using
jdk 13.0.2

The odpe.jar file was tested with this version of Java on the
Windows subsystem for Linux.

If you have issues with correct functionality you may need jdk 13.0.2 
which can be downloaded here:
- https://www.oracle.com/java/technologies/javase-jdk13-downloads.html

Otherwise:
- https://www.java.com/en/download/

Test your version of Java with:
- java -version

Run the .jar file with:
- java -jar odpe.jar

======================================================================
GENERAL
======================================================================
All files within the directory are vital for the functioning of
the open deduction proof editor.
-------------------
<<<<<<< HEAD:Linux/README.txt
SMALL EXPLANATION
=======
INTRODUCTION
>>>>>>> 2b8617010c027118c9188d19c04bdce90af50dee:README.txt
-------------------
Once the application has been launched, either click a button
for the default proof system (SKSg at the time of writing), or
select 'browse', and choose your own, or other's XML maude files.

Enter a formula or derivation, and click 'Okay' (Ctrl + Enter).
You can then highlight the formula, or a subformula, and right click.
Choose to 'Do one proof step', and apply a rule from the list (if
there are any valid rules applications).
-------------------------------------
SIMPLE SCENARIOS ONE MAY WANT TO TRY
-------------------------------------
Here are some scenarios you may try to test the system. Or use your
own. These assume the proof system SKSg.

Scenario 1 - open deduction
- Enter a formula {[a,b],-a}
- Apply the switch rule to the whole formula
- Undo and redo
- Then apply the interaction rule to [a,-a]

Scenario 2 - subatomic logic
- Enter a formula [a,-a]
- Press the 'subatomise' button
- Apply the rule a-down to [a,-a]
- Press the 'interpret' button

Scenario 3 - derivation input
- Enter a >['c_down]> [a,a] and press OK

Scenario 3 - proof search
<<<<<<< HEAD:Linux/README.txt

=======
>>>>>>> 2b8617010c027118c9188d19c04bdce90af50dee:README.txt
- Enter:
(({[a,c],[e,g]} >[Q6]> phi1) a ({[b,d],[f,h]} >[Q7]> phi2)) >[Q5]>
([{a,phi3} a {b,phi4} >[Q4]> {a a b,phi5 a phi6},{c,phi7} a {d,phi8}
>[Q3]> {c a d,phi9 a phi10}] >[Q2]> {[a a b,c a d],{phi11 a phi12,phi13 a phi14} 
>[Q1]> (phi15 a phi16 >[Q]> [e a f,g a h])}) 
<<<<<<< HEAD:Linux/README.txt

=======
>>>>>>> 2b8617010c027118c9188d19c04bdce90af50dee:README.txt
- Press OK
- Press the button 'Proof Search'

======================================================================
OPERATING SYSTEM
======================================================================
Maude works 'out of the box' with Linux and MacOS.
Maude works within the Windows Subsystem for Linux.
---------
Linux:
---------
There are two types of Maude 3.0 files, it does not matter which one
you download, but you must rename it to "maude.linux64".

---------
Mac:
---------
There are two types of Maude 3.0 files, it does not matter which one
you download, but you must rename it to "maude.darwin64".

---------
Windows:
---------
With the introduction of the Windows Subsystem for Linux
this software can be run on Windows. In order to do so,
one must install and enable the Windows Subsystem.

 - https://docs.microsoft.com/en-us/windows/wsl/install-win10

There are two types of Maude 3.0 files, it does not matter which one
you download, but you must rename it to "maude.linux64".

======================================================================
FILE DESCRIPTIONS
======================================================================
 --- maude.linux64 / maude.darwin64 ---
This is the executable Maude program.

 --- prelude.maude ---
The majority of Maude is written in Maude. This is prelude file that
the Maude team provides. It offers the very core functions of Maude.

 --- ksg.maude ---
Maude file for the proof system, defines grammar and rules.

 --- nnf_KS.maude ---
Maude file for canonically simplifying a formula in KSg.

 --- sam.maude ---
Maude file for the subatomic proof system SAKS, includes the object
level strategies for proof searching.

 --- s.maude ---
Maude file with functional modules corresponding to representation
and interpretation of SKS and SAKS, respectively.

 --- util.maude ---
Meta-level Maude file that provides many key utility functions. The GUI
uses this to find rewrites, convert between subatomic systems etc. This
is effectively the main file where all the meta-level computation is
performed.

 --- description.dtd ---
declarations for XML files.

 --- odpe2maude.dtd ---
declarations for XML files.

 --- ksg-maude.xml ---
XML file that stores the description of the proof system, used by the
GUI to correctly display various symbols.


======================================================================
TROUBLESHOOTING
======================================================================
This information is for people who want to compile the code themselves
from source.

Some of the code for parsing is effectively legacy code. Regex was not 
yet released for Java in 2006, when the original GraPE code was last
maintained. It does however still function perfectly. I would like to
completely replace it in the future though.

Note: If you have problems with the stack on linux - you can temporarily 
call
- ulimit -s unlimited

---------------------------------------------------
Editing Maude.java - for future troubleshooting
---------------------------------------------------
In the file Maude.java at line 33,

 - maudecmd[0] = "./maude.linux64"

it must be changed to the name of the Maude file,
therefore to switch between Mac and Linux it must
be done here. (./maude.darwin64 or ./maude.linux64)

In order to use the software on Windows, then because
bash commands can be run from a windows cmd, (if the 
subsystem is installed), line 46 must be changed.

 - ProcessBuilder pb = new ProcessBuilder(maudecmd)

must be replaced by

 - String cmd = "\"".concat(String.join(" ",maudecmd)).concat("\"");
 - ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "bash -c ".concat(cmd));

======================================================================
PROOF SYSTEMS
======================================================================
Proof systems are implemented in Maude and XML.
Please see KSg.maude and KSg-maude.xml for examples.

Within Maude files the structure is as follows:

fmod 
	- Grammar of the proof system
endm

mod
	- Rules of the proof system
endm

======================================================================
FUTURE WORK
======================================================================
- Fix various bugs that occur.
- Improve support for representation of regular logic.
- Completely generalise Maude strategies at the meta-level to support
proof search in other proof systems.
- Rewrite some old code for parsing.
- Rewrite some functions in general for better coding practises.
- Improve error handling.
- Make maude definitions more user-friendly (e.g. extract everything
to the meta-level where the user does not need to see it).

======================================================================
CONTACT
======================================================================
Joe Lynch
joe.r.d.lynch@gmail.com
