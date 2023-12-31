package org.specs2.guide

object DetailedTopics extends UserGuidePage {
  def is = sortedLinks ^ s2"""

 Topic                                                               | See
------------------------------------------------------               | ---------------
Create an example with just some code as the description             | ${link(AutoExamples).mute}
Gather all expectations in an example                                | ${link(GetAllExpectations).mute}
Reference another specification                                      | ${link(ReferenceOtherSpecifications).mute}
Add formatting to a specification                                    | ${link(SpecificationFormatting).mute}
Create HTML tables to specify examples                               | ${link(UseForms).mute}
Create examples "on the fly" as the specification executes           | ${link(CreateOnlineSpecifications).mute}
Create an example with different data displayed in a table           | ${link(UseDatatables).mute}
Fragments API                                                        | ${link(FragmentsApi).mute}
Create an example with different data displayed in a table           | ${link(UseDatatables).mute}
Use $specs2 environment (command-line args, file system...)          | ${link(Environment).mute}
Execution environment for Futures                                    | ${link(ExecutionEnvironments).mute}
Create a new type of Result                                          | ${link(AsResultTypeclass).mute}
Other build tools                                                    | ${link(OtherBuildTools).mute}
Use $specs2 matchers outside of $specs2                              | ${link(matchers.OutsideSpecs2).mute}
Matchers reference card                                              | ${link(matchers.ReferenceCard).mute}
Arguments reference card                                             | ${link(ArgumentsReference).mute}
"""

  def sortedLinks =
    link(AsResultTypeclass).hide ^
      link(ArgumentsReference).hide ^
      link(AutoExamples).hide ^
      link(UseDatatables).hide ^
      link(UseForms).hide ^
      link(CreateOnlineSpecifications).hide ^
      link(Environment).hide ^
      link(ExecutionEnvironments).hide ^
      link(FragmentsApi).hide ^
      link(GetAllExpectations).hide ^
      link(OtherBuildTools).hide ^
      link(matchers.OutsideSpecs2).hide ^
      link(matchers.ReferenceCard).hide ^
      link(SpecificationFormatting).hide ^
      link(ReferenceOtherSpecifications).hide

}
