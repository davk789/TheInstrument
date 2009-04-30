//MIDIClient.init;
s.waitForBoot{
	TheInstrument.loadSynthDefs;
	TheInstrument.new;
	//m = MIDIOut.new(0,MIDIClient.destinations[6].uid);
	~monoSeq = MonoSequencer.new;
	~monoSeq.onsetAction = { |num,vel|
		("monoseq onset action " ++ num).postln;
		TheInstrument.noteOnFunction.(nil, 0, num, vel);
	};
	~monoSeq.releaseAction = { |num|
		TheInstrument.noteOffFunction.(nil, 0, num, 0);
	};
	~monoSeq.sequence = Array.fill(32, { 24.rand + 60 })
};
// seq 1
~monoSeq.sequence = (Array.fill(3, { |ind|
 var rnd, rnd2;
 rnd = (5.rand - 0.25) * 5;
 rnd2 = (5.rand - 0.25) * 5;
 [15, 45, {35 + 70.rand}, {45 + 20.rand}, 35 + rnd2, 25 + rnd, 15, rnd2 + 5, 5, 25, 66, 45, 65, 45, 25, 15, 5];
}).flatten + 40).clip(55,95);
~monoSeq.duration = [0.09,0.1,0.11,0.12,0.13,0.14];
// seq 2
~monoSeq.sequence = Array.fill(4, { |ind|
	var r1, r2;
	r1 = (5.rand * 6) + 60;
	r2 = (4.rand * 7) + 60;
	[55, r1, {(5.rand * 6) + 60}, {r1 + (3.rand * 2)}, 95, {86 + (3.rand * 2)}, 75, 64,
	 68, {(4.rand * 7) + 60}, r2, 82, {r2 + (3.rand * 2)}, 82, {72 + (3.rand * 2)}, 55]
}).flatten;
~monoSeq.duration = [0.1, 0.11, 0.15, 0.09] + 0.1;
// seq 3
~monoSeq.sequence = Array.fill(4, { |ind|
	[55, 65, 78, 86, 54, 63, 75, 82, 49, 57, 67]
}).flatten;
~monoSeq.duration = [0.1,0.1,0.15,0.09];
~monoSeq.length = [0.08, 0.07, 0.13, 0.07];
TheInstrument.noteOnFunction.(0, 0,25,30)