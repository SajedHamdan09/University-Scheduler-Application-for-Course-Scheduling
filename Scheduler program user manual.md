


The program has been compiled into into a JAR file located in the target/ directory
running the FileSelectorWindow from this JAR will start the program.

# File Selection screen

Upon starting the program the user is greeted with the file selector window.
Firstly select the excel file that includes all the necessary information (see below for required Excel Format)


- Note the window may need resizing or user may need to scroll down to see the rest of the elements

![[Pasted image 20230815182508.png]]
if the courses are for the summer semester tick the "**Summer?**" checkbox.
This will double the sessions/week for every course written in the excel file.

Then click schedule to open the Weekly schedule 

## Structure of the excel file

select the excel file that includes 2 sheets; A course info sheet, and an instructor info sheet.

### Course Info sheet:

![[Pasted image 20230813133245.png]]

The first cell of the first column of the course info Sheet must be "coursecode" (not case sensitive). This is to differentiate between the course info sheet and the instructor info sheet.
#### Input values:
##### Course code, Course name, Type:

![[Pasted image 20230813133425.png]]
CourseCode, CourseName, and Type can include any Text as it does not affect the scheduling Logic.


##### Number of Credits:

![[Pasted image 20230813133540.png]]

Any value that is not a purely numbered value will be taken as 0:
E.G.:
"NA" = 0 credits
"3 credits" = 0 credits
The cell must only include the number.

##### Number of sessions:

![[Pasted image 20230813133759.png]]
There are 2 acceptable forms of input for this cell:

- a pure number value
	E.G.: "2"
- a pure number value followed by the word "common"
	E.G.: "4 common"
	this indicates that all the sessions scheduled must be shared at the same exact hour for all the sections of that course. (such as in the case of CMPS210)

##### number of sections:

![[Pasted image 20230813134011.png]]
Similar to the case of number of credits.
Must include a purely numeric value. Any other value will be taken as 0.
"NA" = 0 sections
"3 sections" = 0 sections 

##### Duration:

![[Pasted image 20230813134134.png]]

Acceptable alternatives to "hr" and "mins" are:

"hour", "hours", "hr", "hrs", "h"
"min", "mins", "minutes", "m"

if one of the keywords is present it must be preceded by a number value
E.G.:

- Valid inputs:
	"1 hour 15 minutes"
	"50 min"
	"2 hrs"
- Invalid inputs:
	" hours 15 mins"
	"1:15"


##### Instructor name

![[Pasted image 20230813134624.png]]

It is vital that the instructor name in the **Course Info sheet** matches exactly with the instructor name written in the **Instructors Info sheet**. Otherwise the course will not be scheduled.
- possible discrepancies:
	"**Dr. Mageda Sharafeddine**" =/= "**Mageda Charafeddine**"

##### Restrictions / course conflicts

![[Pasted image 20230813134805.png]]

This list includes the course Codes of other courses that cannot coincide with the main course on the same time slots.
The codes must match exactly with the way they are written.


#### Instructor info sheet:

![[Pasted image 20230813135332.png]]

##### Instructor Name:
Must match exactly with the name written in the **Course Info sheet**

##### Instructor Hours:
can be written either in:
- Military time (ie 24 hour format) 
	E.G.: 
		08:00
		8:55
		17:15
or in
- 12 hour format
	In this case: if the hour is between 1:00 and 6:00, it will be converted to military time
	E.G.: 
		1:30 = 13:30
		02:18 = 14:18

the instructors hours must be split by a " - ".



# Schedule Screen


- Note the window may need resizing or user may need to scroll down to see the rest of the elements

![[Pasted image 20230815184631.png]]

- Each **Time Slot** is its own button, clicking on any time slot opens a list to the right side of the window with all the courses present in that time slot.

- Each course in the course List is its own button. clicking on any course highlights other time slots where this course is also located.

- Scrolling down there are 2 extra buttons

![[Pasted image 20230815184900.png]]

the all courses button includes all of the non Zero session courses (Zero session courses are those that have 0 sessions/week, such as internships and senior projects)

the unscheduled courses button shows a list of all unscheduled courses
![[Pasted image 20230820221157.png]]
these are:
- Zero session courses.
- Courses with an error from reading the course info. such as those with an instructor not found in the instructors sheet
- Courses that remain unscheduled due to the program not finding a suitable timeslot



