+ ArrayedCollection {
	getIndex { |index|
		var ret;
		ret = this.collect({ |obj|
			obj[index];
		});
		^ret;
	}
}

+ Collection {
	weakIncludes { | item1 | 
		this.do {|item2| if (item1 == item2) {^true} };
		^false
	}
}