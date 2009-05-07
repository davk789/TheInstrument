/*
-- fix MonoSequencer GUI bug
*/

//MIDIClient.init;

s.waitForBoot{
	TheInstrument.new;
	//m = MIDIOut.new(0,MIDIClient.destinations[6].uid);
	~monoSeq = MonoSequencer.new;
	~monoSeq.onsetAction = { |num,vel|
		//m.noteOn(1, num, vel);
		TheInstrument.noteOnFunction.(nil, 0, num, vel);
	};
	~monoSeq.releaseAction = { |num|
		//m.noteOff(1, num, 0);
		TheInstrument.noteOffFunction.(nil, 0, num, 0)
	};
	~monoSeq.sequence = Array.fill(32, { 24.rand + 60 })
};
// seq 5
~life = ConwayLife.new(8, GUI.window.new("dsf", Rect.new(500.rand, 500.rand, 150, 150)).front, Rect.new(0,0,140,140));
~lifeVel = ConwayLife.new(8, GUI.window.new("vel", Rect.new(500.rand, 500.rand, 150, 150)).front, Rect.new(0,0,140,140));
~monoSeq.doneAction = { |note,vel|
	postln("doneAction " ++ note ++ " " ++ vel);
	~monoSeq.sequence = 
		(~life.getNextGeneration * 7).flatten + 
		Array.fill(64, { |ind| 
			[0, 0, 0, 0, 2, 2, 0].choose
		}) + 
		43;
	~monoSeq.setSeqVelocities(
		(~lifeVel.getNextGeneration * 90).flatten + 
		Array.fill(64, { |ind| 
			[0, 0, 27, 10, 2, 2, 5, 0, 0].choose
		});	
	);
};
~monoSeq.sequence = { 
	(~life.getNextGeneration * -2).flatten + 
	Array.fill(64, { |ind| 
		[0, 0, 0, 0, 2, 2, 0, 0].choose
	}) + 
	43;
}.value;
~monoSeq.duration = Array.fill(64, {
	{[0.05, 0.1, 0.1, 0.25, 0.1, 0.1, 0.05].choose}
});
~monoSeq.length = Array.fill(64, {
	{[0.05, 0.1, 0.1, 0.25, 0.1, 0.1, 0.05].choose}
});


// seq 4
~monoSeq.sequence = (Array.fill(64, {
	{[0,0,0,0,1,1,-2].choose}
}).flatten + 3) + 25;
~monoSeq.duration = Array.fill(64, {
	{[0.05, 0.1, 0.1, 0.25, 0.1, 0.1, 0.05].choose}
});
~monoSeq.length = Array.fill(64, {
	{[0.05, 0.1, 0.1, 0.25, 0.1, 0.1, 0.05].choose}
});
// seq 1
~monoSeq.sequence = (Array.fill(3, { |ind|
 var rnd, rnd2;
 rnd = (5.rand - 0.25) * 5;
 rnd2 = (5.rand - 0.25) * 5;
 [15, 45, {35 + 70.rand}, {45 + 20.rand}, 35 + rnd2, 25 + rnd, 15, rnd2 + 5, 5, 25, 66, 45, 65, 45, 25, 15, 5];
}).flatten + 40).clip(55,95) - 28;
~monoSeq.duration = [0.09,0.1,0.11,0.12,0.13,0.14];
// seq 2
~monoSeq.sequence = Array.fill(4, { |ind|
	var r1, r2;
	r1 = (5.rand * 6) + 60;
	r2 = (4.rand * 7) + 60;
	[55, r1, {(5.rand * 6) + 60}, {r1 + (3.rand * 2)}, 95, {86 + (3.rand * 2)}, 75, 64,
	 68, {(4.rand * 7) + 60}, r2, 82, {r2 + (3.rand * 2)}, 82, {72 + (3.rand * 2)}, 55]
}).flatten - 24;
~monoSeq.duration = [0.1, 0.11, 0.15, 0.09] + 0.1;
// seq 3
~monoSeq.sequence = Array.fill(4, { |ind|
	var rand1, rand2;
	rand1 = (5.rand - 2) * 4;
	rand2 = (4.rand - 2) * 4;
	[{55 + rand1 + ((4.rand - 2) * 4)}, {65 + rand2 + ((4.rand - 2) * 4)}, {78 + rand1 + ((4.rand - 2) * 4)}, {86 + rand2 + ((4.rand - 2) * 4)}, 
	{55 + rand1 + ((4.rand - 2) * 4)}, {65 + rand2 + ((4.rand - 2) * 4)}, {75 + rand1 + ((4.rand - 2) * 4)}, {82 + rand2 + ((4.rand - 2) * 4)}, 
	{49 + rand1 + ((4.rand - 2) * 4)}, {65 + rand2 + ((4.rand - 2) * 4)}, {76 + rand1 + ((4.rand - 2) * 4)}, {85 + rand2 + ((4.rand - 2) * 4)},
	{49 + rand1 + ((4.rand - 2) * 4)}, {65 + rand2 + ((4.rand - 2) * 4)}, {76 + rand1 + ((4.rand - 2) * 4)}, {85 + rand2 + ((4.rand - 2) * 4)}];
}).flatten.clip(45,95) - 22;
~monoSeq.duration = [0.1,0.1,0.15,0.09] * 2;
~monoSeq.length = [0.08, 0.07, 0.13, 0.07] * 0.2;

s.queryAllNodes

