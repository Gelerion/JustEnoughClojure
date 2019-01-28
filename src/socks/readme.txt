Modeling Sock Transfers

You can model this sock transfer with refs. Refs allow you to update the state of
multiple identities using transaction semantics. These transactions have three features:

  1. They are atomic, meaning that all refs are updated or none of them are.
  2. They are consistent, meaning that the refs always appear to have valid states.
     A sock will always belong to a dryer or a gnome, but never both or neither.
  3. They are isolated, meaning that transactions behave as if they executed serially;
     if two threads are simultaneously running transactions that alter the same ref,
     one transaction will retry. This is similar to the compare-and-set semantics of atoms.

You might recognize these as the A, C, and I in the ACID properties of database
transactions. You can think of refs as giving you the same concurrency safety as
database transactions, only with in-memory data.

Clojure uses software transactional memory (STM) to implement this behavior.
STM is very cool, but when you’re starting with Clojure, you don’t need to know
much about it; you just need to know how to use it, which is what this section shows you.