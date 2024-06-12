# University-Scheduler-Application-for-Course-Scheduling
Collaborating with two peers, I co-developed the University Scheduler Application in Java. It streamlined course scheduling, demonstrating proficiency in Java programming, Excel data handling, GUI development, and problem-solving, simplifying the process.

# File Selection screen

Firstly select the excel file that includes all the necessary information (see below)

![Pasted image 20230815182508](https://github.com/Keyoru/Scheduler-final-rewrite/assets/120273123/e877937c-410a-4931-822a-2341070fd505)


if the courses are for the summer semester tick the "**Summer?**" checkbox.
This will double the sessions/week for every course written in the excel file.

Then click schedule to open the Weekly schedule 

## Structure of the excel file

select the excel file that includes 2 sheets; A course info sheet, and an instructor info sheet.

### Course Info sheet:

![Pasted image 20230813133245](https://github.com/Keyoru/Scheduler-final-rewrite/assets/120273123/367839c3-d2e3-42e4-a53a-7b729383c09d)

The first cell of the first column of the course info Sheet must be "coursecode" (not case sensitive). This is to differentiate between the course info sheet and the instructor info sheet.
#### Input values:
##### Course code, Course name, Type:

![Pasted image 20230813133425](https://github.com/Keyoru/Scheduler-final-rewrite/assets/120273123/5ff8b446-3b5d-4dc0-9bbb-528ba5e55924)

CourseCode, CourseName, and Type can include any Text as it does not affect the scheduling Logic.


##### Number of Credits:

![Pasted image 20230813133540](https://github.com/Keyoru/Scheduler-final-rewrite/assets/120273123/5cea6d36-46c3-407a-9e91-d61a7f59e2c5)

Any value that is not a purely numbered value will be taken as 0:
E.G.:
"NA" = 0 credits
"3 credits" = 0 credits
The cell must only include the number.

##### Number of sessions:

![Pasted image 20230813133759](https://github.com/Keyoru/Scheduler-final-rewrite/assets/120273123/18ced9eb-e1de-4bc1-8248-7450f18eca5d)

There are 2 acceptable forms of input for this cell:

- a pure number value
	E.G.: "2"
- a pure number value followed by the word "common"
	E.G.: "4 common"
	this indicates that all the sessions scheduled must be shared at the same exact hour for all the sections of that course. (such as in the case of CMPS210)

##### number of sections:

![Pasted image 20230813134011](https://github.com/Keyoru/Scheduler-final-rewrite/assets/120273123/5e1f14f9-4db3-421d-b839-d59de88f536e)

Similar to the case of number of credits.
Must include a purely numeric value. Any other value will be taken as 0.
"NA" = 0 sections
"3 sections" = 0 sections 

##### Duration:

![Pasted image 20230813134134](https://github.com/Keyoru/Scheduler-final-rewrite/assets/120273123/33109e35-6210-47c8-8249-839b24ad084c)


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

![Pasted image 20230813134624](https://github.com/Keyoru/Scheduler-final-rewrite/assets/120273123/679a8b4e-58e3-4614-8a61-958e51f254da)

It is vital that the instructor name in the **Course Info sheet** matches exactly with the instructor name written in the **Instructors Info sheet**. Otherwise the course will not be scheduled.
- possible discrepancies:
	"**Dr. Mageda Sharafeddine**" =/= "**Mageda Charafeddine**"

##### Restrictions / course conflicts

![Pasted image 20230813134805](https://github.com/Keyoru/Scheduler-final-rewrite/assets/120273123/7d0ebcaf-b727-45c9-9b61-d10d4764bde0)

This list includes the course Codes of other courses that cannot coincide with the main course on the same time slots.
The codes must match exactly with the way they are written.


#### Instructor info sheet:

![Pasted image 20230813135332](https://github.com/Keyoru/Scheduler-final-rewrite/assets/120273123/6f6644a1-49b4-4c6a-b27d-7fdaf92ba4fa)

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

![Pasted image 20230815184631](https://github.com/Keyoru/Scheduler-final-rewrite/assets/120273123/7a51790f-318c-4ed4-b449-86d93e41d8ba)

- Each **Time Slot** is its own button, clicking on any time slot opens a list to the right side of the window with all the courses present in that time slot.

- Each course in the course List is its own button. clicking on any course highlights other time slots where this course is also located.

- Scrolling down there are 2 extra buttons

![Pasted image 20230815184900](https://github.com/Keyoru/Scheduler-final-rewrite/assets/120273123/0e4d6fe1-82cf-4cda-b1c4-c47d218c9f27)

the all courses button includes all of the non Zero session courses (Zero session courses are those that have 0 sessions/week, such as internships and senior projects)

the unscheduled courses button shows a list of all unscheduled courses

![Pasted image 20230815185638](https://github.com/Keyoru/Scheduler-final-rewrite/assets/120273123/1d016f47-7f65-4a8e-a0da-524268cc412c)

these are:
- Zero session courses.
- Courses with an error from reading the course info. such as those with an instructor not found in the instructors sheet
- Courses that remain unscheduled due to the program not finding a suitable timeslot



# Scheduler-final-rewrite
