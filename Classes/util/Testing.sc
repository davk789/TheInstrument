TestClass {
	var buffer, server;
	*new {
		^super.new.init_testclass;
	}
	
	init_testclass {
		server = Server.default;
		buffer = Buffer.read(server, "/Users/playmac/Music/sc-test-clips/082606-1-bhdk.norm render 002.wav");
	}
	
	getBufferValues {
		1024.do{ |ind|
			buffer.get(ind, { |msg| msg.postln; });
		}
	}
}