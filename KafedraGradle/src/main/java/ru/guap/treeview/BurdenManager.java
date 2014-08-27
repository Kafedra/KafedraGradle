package ru.guap.treeview;

import ru.guap.dao.DBManager;

import com.jsptree.bean.Node;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Класс для формирования дерева для отображения дисциплин, групп, потоков и видов нагрузки
 * @author Cr0s
 *
 */
public class BurdenManager {

	/**
	 * Таблица для соответствия id нагрузки с элементом нагрузки
	 */
	private HashMap<Integer, GroupLoadItem> items;
	
	/**
	 * Список дисциплин осеннего семестра
	 */
	private LinkedList<Discipline> discsListAutumn;
	
	/**
	 * Список дисциплин весеннего семестра
	 */
	private LinkedList<Discipline> discsListSpring;
	
	/**
	 * Ссылка на экземпляр класса
	 */
	private static BurdenManager instance;

	/**
	 * Таблица соответствий стандартных названий видов нагрузок и их читаемых форм
	 */
	public static HashMap<String, String> loadKindRenamingTable;
	
	/**
	 * Стандартное обозначение лекции
	 */
	private static String RU_LECTURE = "лекц";
	
	/**
	 * Статически инициализируем (заполняем) таблицу
	 */
	static {
		loadKindRenamingTable = new HashMap<>();

		loadKindRenamingTable.put(RU_LECTURE, "Лекция");
		loadKindRenamingTable.put("практ", "Практика");
		loadKindRenamingTable.put("лаб", "Лабораторная");
		loadKindRenamingTable.put("КР", "Курсовая работа");
		loadKindRenamingTable.put("КП", "Курсовой проект");
		loadKindRenamingTable.put("контр.", "Контрольная");
		loadKindRenamingTable.put("зач", "Зачёт");
		loadKindRenamingTable.put("экз.конс.", "Консультация");
		loadKindRenamingTable.put("экз", "Экзамен");
	}

	/**
	 * Корни деревьев для двух семестров
	 */
	static Node rootAuthum = null;
	static Node rootSpring = null;

	/**
	 * SQL-выражения для работы с СУБД
	 */
	static PreparedStatement selectDiscNamesAuthum;
	static String selectDiscNamesAuthumSql = "SELECT DISTINCT NameDisc, CodeDisc FROM kafedra.kaf43 WHERE ((mod(Nsem, 2) != 0) AND (load_id = ?))";

	static PreparedStatement selectDiscNamesSpring;
	static String selectDiscNamesSpringSql = "SELECT DISTINCT NameDisc, CodeDisc FROM kafedra.kaf43 WHERE ((mod(Nsem, 2) = 0) AND (load_id = ?))";

	static PreparedStatement selectGroupsAuthum;
	static String selectGroupsAuthumSql = "SELECT `Group`, NStream, KindLoad, id, teachers_id, ValueEP, ValueG, ValueCO, ValueEP, NameDisc FROM kafedra.kaf43 WHERE (mod(Nsem, 2) != 0) AND (load_id = ?) AND (NameDisc = ?)";

	static PreparedStatement selectGroupsSpring;
	static String selectGroupsSpringSql = "SELECT `Group`, NStream, KindLoad, id, teachers_id, ValueEP, ValueG, ValueCO, ValueEP, NameDisc FROM kafedra.kaf43 WHERE (mod(Nsem, 2) = 0) AND (load_id = ?) AND (NameDisc = ?)";	

	static PreparedStatement appointmentCheck;
	static final String appointmentCheckSql = "SELECT teachers_id FROM kafedra.kaf43 WHERE (id = ?) AND (load_id = ?)";

	/**
	 * Номер версии нагрузки
	 */
	// TODO: сделать загрузку номера версии из файла конфигурации
	public static final int LOAD_VERSION = 1;

	/**
	 * Закрытый конструктор экземпляра класса, проводит начальную инициализацию структур данных. Реализует паттерн "Singleton".
	 */
	private BurdenManager() {
		this.items = new HashMap<>();
		this.discsListAutumn = new LinkedList<>();
		this.discsListSpring = new LinkedList<>();
		
		// XXX: предзагрузка узлов не используется
		/*try {
			getRootNode(true);
			getRootNode(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}*/
	}	
	
	/**
	 * Синхронизированный метод получения экземпляра класса. Реализует паттерн "Singleton".
	 * @return экземпляр класса
	 */
	public synchronized static BurdenManager getInstance() {
		if (instance == null) {
			instance = new BurdenManager();
		}

		return instance;
	}

	/**
	 * Получает элемент потока по идентификатору потока и идентификтору дисциплины
	 * @param streamId идентификатор потока
	 * @param discId идентификатор дисциплины
	 * @return элемент потока
	 */
	public GroupStream getStreamForDiscAndId(int streamId, int discId) {
		// Просмотр осеннего семестра
		List<Discipline> discs = (List<Discipline>) this.getDiscsById(discId, true);
		
		for (Discipline disc : discs) {
			GroupStream stream = disc.getSteams().get(streamId);
			
			if (stream != null) {
				return stream;
			}
		}
		
		// Просмотр весеннего семестра
		discs = (LinkedList<Discipline>) this.getDiscsById(discId, false);
		for (Discipline disc : discs) {
			GroupStream stream = disc.getSteams().get(streamId);
			
			if (stream != null) {
				return stream;
			}
		}
		
		return null;
	}
	
	/**
	 * Получает элемент нагрузки по его идентификатору
	 * @param id идентификатор элемента нагрузки
	 * @return элемент нагрузки
	 */
	public GroupLoadItem getLoadItemById(int id) {
		return this.items.get(id);
	}
	
	/**
	 * Получает список дисциплин по идентификатору
	 * Один идентификатор может относиться к нескольким дисциплинам.
	 * @param discId идентификатор дисциплины
	 * @param isAutumn признак осеннего семестра, если false - то весенний
	 * @return список дисциплин
	 */
	public List<Discipline> getDiscsById(int discId, boolean isAutumn) {	
		LinkedList<Discipline> list = (isAutumn) ? discsListAutumn : discsListSpring;
		LinkedList<Discipline> res = new LinkedList<>();
	
		for (Discipline disc : list) {
			if (disc.id == discId) {
				
				res.add(disc);
			}
		}
		
		return res;
	}

	/**
	 * Возвращает кореневой элемент древовидной структуры
	 * @param isAutumn признак осеннего семестра, если false - то весеннего
	 * @return корневой узел дерева
	 * @throws SQLException исключение об ошибке работы с СУБД
	 */
	public Node getRootNode(boolean isAutumn) throws SQLException {
		// XXX: Кеширование не используется
		/*if (isAutumn) {
			//if (rootAuthum == null) {
				rootAuthum = getTreeFromDb(true, LOAD_VERSION);
			//}

			return rootAuthum;
		} else {
			//if (rootSpring == null) {
				rootSpring = getTreeFromDb(false, LOAD_VERSION);
			//}

			return rootSpring;
		}*/
		
		return getTreeFromDb(isAutumn, LOAD_VERSION);
	}

	/**
	 * Формирует древовидную структуру и возвращает её корневой элемент
	 * @param isAutumn признак осеннего семестра, если false - то весеннего
	 * @param version номер версии данных о нагрузке
	 * @return корневой узел дерева
	 * @throws SQLException исключение об ошибке работы с СУБД
	 */
	private Node getTreeFromDb(boolean isAutumn, int version) throws SQLException {
		Connection cnn = DBManager.getInstance().getConnection();

		Node root = new Node("root");
		root.setIsSelected(0);
		root.setErrorDescription("");

		// Выполняем SQL запрос и получаем список дисциплин
		ArrayList<Discipline> discs = new ArrayList<>();
		if (isAutumn) {
			if (selectDiscNamesAuthum == null) {
				selectDiscNamesAuthum = cnn.prepareStatement(selectDiscNamesAuthumSql);
			}

			getDiscNames(selectDiscNamesAuthum, discs, version);
		} else {
			if (selectDiscNamesSpring == null) {
				selectDiscNamesSpring = cnn.prepareStatement(selectDiscNamesSpringSql);
			}

			getDiscNames(selectDiscNamesSpring, discs, version);			
		}

		// Таблица сопоставления кода дисциплины и её элемента
		HashMap<Integer, Discipline> discByCode = new HashMap<>();

		// Вставляем дисциплины в дерево
		for (Discipline disc: discs) {
			// Запоминаем отношение код->дисциплина
			if (!discByCode.containsKey(disc.id)) {
				discByCode.put(disc.id, disc);
			}

			// Получаем список групп, которым назначена эта дисциплина с этим именем
			List<GroupLoadItem> groupsList = (List<GroupLoadItem>) getGroupsForDiscipline(disc.name, version, isAutumn);

			// Создаём "Нулевой поток", в который попадают группы, не находящиеся в каком-либо потоке
			GroupStream zeroStream = new GroupStream(true, 0);

			// Перебираем группы
			for (GroupLoadItem item : groupsList) {
				if (item.getStreamId() == 0) { 
					// Группа не в потоке, вносим её в нулевой поток
					zeroStream.addGroupItem(item);
				} else {
					// Находим соответствующий группе поток
					GroupStream stream = null;
					
					// Если это лекция, то получаем дисциплину по её коду и 
					// получаем поток, связанный с этой дисциплиной
					
					boolean isLecture = item.getKindLoad().equals(RU_LECTURE);
					if (isLecture) {
						// Для потоковой лекции поиск осуществляется по коду дисциплины, так как 
						// существуют случаи, когда дисциплины имеют один код,
						// но разные названия						
						stream = discByCode.get(disc.id).getSteams().get(item.getStreamId());
					} else {
						// Иначе получаем поток для текущей дисциплины
						stream = disc.getSteams().get(item.getStreamId());
					}

					// Если поток уже был сформирован и добавлен, то
					// вносим в него текущую группу
					if (stream != null) {
						stream.addGroupItem(item);
					} else {
						// Иначе формируем объект потока и вносим в него текущий элемент группы
						stream = new GroupStream(false, item.getStreamId());
						stream.addGroupItem(item);

						// Добавляем к дисциплине новый поток
						if (isLecture) {
							// Устанавливаем признак того, что в потоке >1 группы
							stream.setMultiGroup(true);
							discByCode.get(disc.id).addStream(stream);
						} else {
							disc.addStream(stream);
						}
					}
				}
			}

			// Устанавливем нулевой поток для дисциплины
			disc.setZeroStream(zeroStream);
		}

		// Формируем дерево на основе списков дисциплин
		for (Discipline disc : discs) {
			// Создаём узел дисциплины
			Node discNode = new Node(new Integer(disc.id).toString(), disc.name);
			discNode.setIsSelected(0);
			discNode.setErrorDescription("");			

			// Преобразуем потоки в узлы и прикрепляем к узлу дисциплины
			for (GroupStream stream : disc.getSteams().values()) {
				addStreamToDisc(stream, disc, discNode);
			}

			// Также прикрепляем нулевой поток
			addStreamToDisc(disc.getZeroStream(), disc, discNode);

			// Прикрепляем узел дисциплины к корню
			root.addChildNode(discNode);
			
			// Добавляем дисциплину в список в соответствии с семестром
			if (isAutumn) {
				this.discsListAutumn.addLast(disc);
			} else {
				this.discsListSpring.addLast(disc);
			}
		}

		return root;
	}

	/**
	 * Добавляет поток к дисциплине
	 * @param stream объект потока
	 * @param disc объект дисциплины
	 * @param discNode узел дерева, связанный с дисциплиной
	 */
	private void addStreamToDisc(GroupStream stream, Discipline disc, Node discNode) {
		// Формируем узел потока
		Node streamNode = new Node(new Integer(stream.getId()).toString(), stream.toString());
		streamNode.setIsSelected(0);
		streamNode.setAppointed(false);
		streamNode.setLoadNode(false);

		// Таблица соответствия вида нагрузки и узла
		HashMap<String, Node> loadKindsNodes = new HashMap<>();

		Node groupLoadNode = null;
		// Если поток состоит из нескольких групп
		if (stream.isMultiGroup()) {
			groupLoadNode = new Node("0", "");
			groupLoadNode.setLoadNode(true);
			groupLoadNode.setIsSelected(0);	
			StringBuilder name = new StringBuilder();
			
			// Добавляем названия групп через запятую
			for (int i = 0; i < stream.getItems().size(); i++) {
				GroupLoadItem item = stream.getItems().get(i);
				
				name.append(item.getName());

				// Задаем признак "нагрузка назначена"
				// Не назначена: если хоть один потомок узла группы не назначен
				// Назначена: если узел не назначен, а группа назначена
				// Следствие: если назначены все потомки узла, то узел тоже назначен
				if (groupLoadNode.isAppointed() && !item.isAppointed()) { 
					groupLoadNode.setAppointed(false);
				} else if (!groupLoadNode.isAppointed() && item.isAppointed()){
					groupLoadNode.setAppointed(true);
				}

				if (i < stream.getItems().size() - 1) {
					name.append(", ");
				}
			}

			groupLoadNode.setStreamId(stream.getId());
			groupLoadNode.setNodeName(name.toString());
			groupLoadNode.setDiscId(disc.id);
			groupLoadNode.setMultiNode(true);
			
			groupLoadNode.setStream(stream);
		}

		// Обработка случаев, когда поток состоит из одной группы
		for (GroupLoadItem i : stream.getItems()) {
			if (!stream.isMultiGroup()) {
				groupLoadNode = new Node(new Integer(i.getId()).toString(), i.getName());
				groupLoadNode.setLoadNode(true);
				groupLoadNode.setAppointed(i.isAppointed());
				groupLoadNode.setIsSelected(0);
				
				groupLoadNode.setItem(i);
			}
			
			Node loadKindNode = loadKindsNodes.get(i.getKindLoad());

			// Создаём узел дерева, если он ещё не создан
			// и преобразуем стандартное обозначение вида нагрузки в читаемый вид			
			if (loadKindNode == null) {
				loadKindNode = new Node("0", loadKindRenamingTable.get(i.getKindLoad()));
				loadKindsNodes.put(i.getKindLoad(), loadKindNode);

				streamNode.addChildNode(loadKindNode);
			}

			// Прикрепляем узел групповой нагрузки к узлу вида нагрузки
			loadKindNode.addChildNode(groupLoadNode);
			
			if (stream.isMultiGroup()) {
				break;
			}
		}

		// Наконец, прикрепляем узел всего потока к узлу дисциплины 
		discNode.addChildNode(streamNode);
	}

	/**
	 * Получает список групп по имени дисциплины
	 * @param discName имя дисциплины
	 * @param version номер версии данных нагрузки
	 * @param isAuthumn осенний/весенний семестр
	 * @return список групп
	 * @throws SQLException ошибка работы с СУБД
	 */
	private List<GroupLoadItem> getGroupsForDiscipline(String discName, int version, boolean isAuthumn) throws SQLException {
		LinkedList<GroupLoadItem> result = new LinkedList<>();

		Connection cnn = DBManager.getInstance().getConnection();

		PreparedStatement ps;
		if (isAuthumn) {
			ps = selectGroupsAuthum;

			if (ps == null) {
				ps = cnn.prepareStatement(selectGroupsAuthumSql);
			}
		} else {
			ps = selectGroupsSpring;

			if (ps == null) {
				ps = cnn.prepareStatement(selectGroupsSpringSql);
			}
		}

		ps.setInt(1, version);
		ps.setString(2, discName);

		ResultSet rs = ps.executeQuery();

		while (rs.next()) {
			String teacherName = DBManager.getInstance().getTeacherById(rs.getInt(5));
			GroupLoadItem item = new GroupLoadItem(rs.getString(1), rs.getInt(2), rs.getString(3), rs.getInt(4), rs.getInt(5) != 0, rs.getInt(5), teacherName);
			
			item.setValueEP(rs.getInt(6));
			item.setValueG(rs.getInt(7));
			item.setValueCO(rs.getInt(8));
			item.setValueCF(rs.getInt(9));
			
			item.nameDisc = rs.getString(10);
			
			result.add(item);
		}

		return result;		
	}

	/**
	 * Получает список дисциплин
	 * @param ps SQL-запрос на получение списка
	 * @param discNames результирующий список
	 * @param version версия нагрузки
	 * @throws SQLException
	 */
	private void getDiscNames(PreparedStatement ps, ArrayList<Discipline> discNames, int version) throws SQLException {
		ps.setInt(1, version);

		ps.execute();
		ResultSet res = ps.getResultSet();

		while (res.next()) {
			discNames.add(new Discipline(res.getString(1), res.getInt(2)));
		}
	}

	/**
	 * Преобразует стандартное именование типа нагрузки в читаемый вид
	 * @param old стандартное именование
	 * @return читаемый вид нагрузки
	 */
	public String renameLoadKind(String old) {
		return loadKindRenamingTable.get(old);
	}
}
