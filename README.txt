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
GENERAL
======================================================================
All files within the directory are vital for the functioning of
the open deduction proof editor.

-------------------------------
SCENARIOS ONE MAY WANT TO TRY
-------------------------------
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
 - Apply the rule a-down to [ff a tt, tt a ff]
 - Press the 'interpret' button

Scenario 3 - derivation input
 - Enter [example derivation here] and press OK

Scenario 3 - proof search
 - Enter [example derivation here] and press OK
 - Press the button 'Proof Search'

======================================================================
GUIDE
======================================================================
You must have Maude 3.0 (or above) installed on your system.
You can download it at the link below, see the section
OPERATING SYSTEM, especially if you are on Windows.

 - http://maude.cs.illinois.edu/w/index.php/The_Maude_System

Please rename the file to 'maude.linux64' for Linux, and
'maude.darwin64' for MacOS. [See OPERATING SYSTEM].

Java must also be on your system, and correctly configured.

 - https://www.java.com/en/download/

Run the .jar file with 

 - java -jar odpe.jar

-------------------
Small explanation
-------------------
Once the application has been launched, either click a button
for the default proof system (SKSg at the time of writing), or
select 'browse', and choose your own, or other's XML maude files.

Enter an formula or derivation, and click 'Okay' (Ctrl + Enter).
You can then highlight the formula, or a subformula, and right click.
Choose to 'Do one proof step', and apply a rule from the list (if
there are any valid rules applications).

======================================================================
PROOF SYSTEMS
======================================================================
Proof systems are implemented in Maude and XML.
Please see KSg.maude and KSg-maude.xml for examples.
This is further described in Ozans work [link].

Within Maude files the structure is as follows:

fmod 
	- Grammar of the proof system
endm

mod
	- Rules of the proof system
endm

======================================================================
OPERATING SYSTEM
======================================================================
Maude works 'out of the box' with Linux and MacOS.
Maude works within the Windows Subsystem for Linux.
---------
Linux:
---------
There are two types of Maude 3.0 files, it does not matter which one
you download, but you must rename it to

 - maude.linux64

---------
Mac:
---------
There are two types of Maude 3.0 files, it does not matter which one
you download, but you must rename it to

 - maude.darwin64

---------
Windows:
---------
With the introduction of the Windows Subsystem for Linux
this software can be run on Windows. In order to do so,
once must install and enable the Windows Subsystem.

 - https://docs.microsoft.com/en-us/windows/wsl/install-win10

There are two types of Maude 3.0 files, it does not matter which one
you download, but you must rename it to

 - maude.linux64

======================================================================
TROUBLESHOOTING
======================================================================
This information is for people who are trying to figure out exactly
how the code works. Some of the code for parsing is effectively legacy
code. Regex was not yet released for Java in 2006, when the original
GraPE code was last maintained. It does however still function
perfectly. I would like to update it in the future though.

---------------------------------------------------
Editing Maude.java - for future troubleshooting
---------------------------------------------------
In the file Maude.java at line 33,

 - maudecmd[0] = "./maude.linux64"

it must be changed to the name of the Maude file,
therefore to switch between Mac and Linux it must
be done here.

In order to use the software on Windows, then because
bash commands can be run from a windows cmd, (if the 
subsystem is installed), line 46 must be changed.

 - ProcessBuilder pb = new ProcessBuilder(maudecmd)

must be replaced by

 - String cmd = "\"".concat(String.join(" ",maudecmd)).concat("\"");
 - ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "bash -c ".concat(cmd));

======================================================================
FUTURE WORK
======================================================================
- Fix various bugs that occur.
- Improve support for representation of regular logic.
- Completely generalise Maude strategies at the meta-level to support
proof search in other proof systems.
- Rewrite some old code for parsing.
- Rewrite some functions in general for better coding practises
- Improve error handling
- Make maude definitions more user-friendly (e.g. extract everything
to the meta-level where the user does not need to see it)

======================================================================
CONTACT
======================================================================
Joe Lynch
joe.r.d.lynch@gmail.com
jl2553@bath.ac.uk