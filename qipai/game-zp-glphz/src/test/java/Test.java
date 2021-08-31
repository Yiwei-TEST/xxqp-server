import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Test {

	
	public static void main(String[] args) {
//		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
//		map.put(1, 1);
//		map.put(2, 1);
//		map.put(3, 1);
//		map.put(4, 1);
//		Map<Integer, Integer> jxamap = new HashMap<>(); 
//		while (map.size() > 0) {
//			for (Integer id : map.keySet()) {
//				if(id == 3){
//					jxamap.put(5, 1);
//				}
//				System.out.println(id);
//			}
//			map.clear();
//			map.putAll(jxamap);
//			jxamap.clear();
//		}
//		System.out.println("end...");
		Set<Integer> set = new HashSet<>();
		set.add(1);
		set.add(2);
		set.add(3);
		set.add(3);
		set.add(4);
		Set<Integer> jxset = new HashSet<>();
		Iterator<Integer> it = set.iterator();
		while (set.size() > 0) {
			for (Integer id :set) {
				if(id == 3){
					jxset.add(5);
				}
				System.out.println(id);
			}
			set.clear();
			set.addAll(jxset);
			jxset.clear();
		}
		System.out.println("end...");
	}
	
}
