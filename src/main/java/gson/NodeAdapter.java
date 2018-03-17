/*
 * Copyright 2018 Sebastian Proksch
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import model.Node;

public class NodeAdapter extends TypeAdapter<Node> {

	private static final String $ID = "$id";
	private static final String $REF = "$ref";

	private Map<Node, Integer> written = new HashMap<>();
	private Map<Integer, Node> read = new HashMap<>();

	@Override
	public void write(JsonWriter out, Node n) throws IOException {
		out.beginObject();
		if (written.containsKey(n)) {
			int id = written.get(n);
			out.name($REF);
			out.value(id);
		} else {
			int id = written.size();
			out.name($ID);
			out.value(id);
			written.put(n, id);

			out.name("value");
			out.value(n.value);

			if (!n.parents.isEmpty()) {
				out.name("parents");
				out.beginArray();
				for (Node p : n.parents) {
					write(out, p);
				}
				out.endArray();
			}

		}
		out.endObject();
	}

	@Override
	public Node read(JsonReader in) throws IOException {
		in.beginObject();
		String n = in.nextName();
		if ($ID.equals(n)) {
			int id = in.nextInt();

			in.nextName(); // value
			int value = in.nextInt();
			List<Node> parents = new LinkedList<>();

			if (in.peek() == JsonToken.NAME) {
				in.nextName(); // parents
				in.beginArray();

				while (in.peek() == JsonToken.BEGIN_OBJECT) {
					parents.add(read(in));
				}

				in.endArray();
			}
			in.endObject();

			Node node = new Node(value);
			node.parents = parents;

			read.put(id, node);
			return node;

		} else if ($REF.equals(n)) {
			int id = in.nextInt();
			in.endObject();

			if (read.containsKey(id)) {
				return read.get(id);
			}
			throw new RuntimeException("unknown reference: '" + id + "'");
		} else {
			throw new RuntimeException("unexpected first token: '" + n + "'");
		}
	}

	public void reset() {
		written.clear();
		read.clear();
	}
}