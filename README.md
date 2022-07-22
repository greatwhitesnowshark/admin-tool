# AdminTool
An administrative tool used for various inspection and reporting.

The purpose of this application -
  1) To analyze and compare massive amounts of SQL table data
  2) To determine whether or not users are guilty of violating game rules (multi-account, item duplicating, etc)
  3) Very neatly and quickly process the large number of tasks, outputting files in different formats to display the results
  
This application was used to track every user that logged in using a shared device, using my own coded logic but we can call it AI here, to determine
a person's true activity even when trying to evade in-game security systems using methods like HWID-spoofing.

It was also used to determine which users were holding suspicious quantities of specific items, and uses a variety of methods to determine if an
account is most likely involved in Real-Money-Trading, the act of selling in-game items or money to others for real life currency via Paypal or similar.

This application is no longer useful for any production measures, since the target application it was designed for has been discontinued and I am no longer
responsible for its database or the users stored within. (thank god) =) 


It is simply here for learning purposes, and to demonstrate the various methods that you can use for intricate SQL table lookups when tables have literally
millions of rows of values each. Fun sh*t. 


Example output of an "Account-lookup", which basically checks a user for all possible aliases and alternate logins by cross referencing their login
records with... many other things... [lucky159-Layloi.txt](https://github.com/greatwhitesnowshark/admin-tool/files/9165338/lucky159-Layloi.txt)

Example output of an "Items-lookup", which checks all of the items that are in an account's possession [so it checks ALL users on their account, there could be up to 50 users on a single account, each with their own item inventories] - then analyzes the item properties and prints out a comprehensive list with specific details and more information - [lalaelmo123.txt](https://github.com/greatwhitesnowshark/admin-tool/files/9165347/lalaelmo123.txt)


Yeah, it's already pretty impressive. I know. Useless now but nobody could hide from me! >:D
Hackers & exploiters of games can go die.


Designed for <Specific App>, 2020-death
