/*
	Sequencer take 2
	this is going to re-se much of the event looper code, but has two main requirements at the moment.
	1 - it needs an interface similar to the SampleLooper (complete with custom SequenceView 
		using MarkerBar)
	2 - each channel needs to be assignable. That is: somehow there needs to be a way to set a looper object
		itself to control other loopers, rather than using the kludgy and non-functional MetaSequence that
		I have right now.
		
*/
SequenceLooper {
	*new {
		^super.new.init_sequencelooper;
	}
	
	init_sequencelooper {
		postln("initialized sequence looper");
	}
}