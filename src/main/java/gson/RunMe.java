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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import model.Node;
import model.NodeContainer;

public class RunMe {

	private static Gson gson;
	private static NodeAdapter typeAdapter;

	public static void main(String[] args) {

		typeAdapter = new NodeAdapter();
		gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Node.class, typeAdapter).create();

		writing();
		reading();
	}

	private static void writing() {
		serialize(new Node(1));
		serialize(new Node(1));
		serialize(new Node(1, new Node(2)));

		Node a = new Node(1);
		Node b = new Node(2, a);
		Node c = new Node(3, a);
		Node d = new Node(4, b, c);
		serialize(d);

		serialize(new NodeContainer(a, a));
	}

	private static void reading() {
		assertEquals(new Node(1), "{\"$id\": 0, \"value\": 1}", Node.class);
		assertEquals(new Node(1, new Node(2)), "{\"$id\": 0, \"value\": 1, \"parents\": [{\"$id\": 1, \"value\": 2}]}",
				Node.class);
		Node n1 = new Node(2);
		assertEquals(new Node(1, n1, n1),
				"{\"$id\": 0, \"value\": 1, \"parents\": [{\"$id\": 1, \"value\": 2}, { \"$ref\": 1}]}", Node.class);

		assertEquals(new NodeContainer(n1, n1), "{ \"a\": {\"$id\": 0, \"value\": 2}, \"b\": {\"$ref\": 0} }",
				NodeContainer.class);

	}

	private static <T> void assertEquals(Object expected, String json, Class<T> type) {
		Object actual = deserialize(json, type);
		if (expected.equals(actual)) {
			System.out.printf("ok: %s\n", json);
		} else {
			System.err.printf("Not equal (%s)! Expected\n\t%s\nbut got\n\t%s\n", json, expected, actual);
		}
	}

	private static <T> T deserialize(String json, Class<T> type) {
		typeAdapter.reset();
		System.out.println("--------------------------");
		return gson.fromJson(json, type);
	}

	private static void serialize(Object o) {
		typeAdapter.reset();
		System.out.println("--------------------------");
		System.out.println(gson.toJson(o));
	}
}