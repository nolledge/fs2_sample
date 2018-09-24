# Fs2 Sample

In this project I wanted to take a look at cats and fs2

## Content: Lottery Case Study

Consider lottery rules, where in a normal ticket
field the customer can select 5 out of 50 numbers and 2 out of 11 "star"-numbers
In a system ticket field the customer can select up to 10
out of 50 numbers and up to 5 out of 11 "star"-numbers leading to up to 2520 field combinations.


## Implementation Details

This is build on top of 

* [The cats library](https://github.com/typelevel/cats)
* [fs2 streams](https://github.com/functional-streams-for-scala/fs2)
* [Circe](https://github.com/circe/circe)

Using a 'tagless final approach'

For testing:

* [Scalacheck](https://www.scalacheck.org/)
* [Scalatest](http://www.scalatest.org/)

I used a mixture of unit testing with fixture data and generated data with scalacheck.

## Parts of the Case Study

## Expansion

This project calculates every possible expansion of a system ticket into a normal ticket in a pure fashion.


*For testing:* the amount of expanded tickets has to equal: n choose 5 times s choose 2
This is calculated with the binomial coefficient implemented in the `CaseStudySpec.scala`

### Expansion of data from file

This is where side effects come in. Fs2 is used to stream a file in which JSONs are stored separated by lines.
I used cats Validation to (defined in validations package and used in the EuroMillions model). To verify the _external_ 
input data. Invalid data is ignored.

Manipulate `testdata/system_tickets.txt` to try it out.


*For testing:* The main program is wrapped in cats.IO. For Testing purposes I have implemented a
TestIO[A] (which is in fact a StateT[IO, Testdata, A]) to capture the console outs and check if the count corresponds
with the ticket expansions.

### Aggregation of draw and ticket data to present prize level overview

Draw result is stored in `testdata/draw_result.txt`. A single line is expected (demo data provided). Mixed (system and normal) tickets are stored in `testdata/mixed_tickets.txt`. Its automatically detected whether it is a system or a normal ticket. System Tickets will be expanded.

These two sources are combined to create an overview of the prize levels and the count of the corresponding tickets.

*For testing:* Again the state monad is used to capture and verify the result in a test scenario.
