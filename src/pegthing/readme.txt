Code Organization
The program has to handle four major tasks, and the source code is organized accordingly,
with the functions for each of these tasks grouped together:

 - Creating a new board
 - Returning a board with the result of the player’s move
 - Representing a board textually
 - Handling user interaction

Two more points about the organization: First, the code has a basic architecture,
or conceptual organization, of two layers. The top layer consists of the functions
for handling user interaction. These functions produce all of the program’s side effects,
printing out the board and presenting prompts for player interaction. The functions
in this layer use the functions in the bottom layer to create a new board, make moves,
and create a textual representation, but the functions in the bottom layer don’t
use those in the top layer at all. Even for a program this small, a little architecture
helps make the code more manageable.

https://www.braveclojure.com/functional-programming/ chapter 5