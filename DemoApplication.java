import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.Font;

public class DemoApplication {

	private JFrame frame;
	private JTextField tableName;
	private JButton btnA;
	private JButton btnB;
	private JButton btnC;
	private JButton btnD;
	private JButton btnE;
	private JButton btnF;
	private JButton btnG;
	private JButton btnH;
	private JButton btnCreate;
	private Relation R;
	private JTextField attributeNames;
	private JTextArea txtQueryExecutionPlan, txtQueryExecutionPlan2;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DemoApplication window = new DemoApplication();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public DemoApplication() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1012, 617);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblEnterFileLocation = new JLabel("Create Table");
		lblEnterFileLocation.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblEnterFileLocation.setBounds(138, 16, 122, 20);
		frame.getContentPane().add(lblEnterFileLocation);
		
		tableName = new JTextField();
		tableName.setFont(new Font("Tahoma", Font.PLAIN, 20));
		tableName.setBounds(192, 52, 224, 38);
		frame.getContentPane().add(tableName);
		tableName.setColumns(10);
		
		btnA = new JButton("Insert City Data");
		btnA.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				readFile("city");
			}
		});
		btnA.setBounds(47, 236, 176, 29);
		frame.getContentPane().add(btnA);
		
		btnCreate = new JButton("Create");
		btnCreate.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					R = new Relation(tableName.getText(),attributeNames.getText());
					tableName.setText("");
					attributeNames.setText("");
			}
		});
		btnCreate.setBounds(47, 191, 108, 29);
		frame.getContentPane().add(btnCreate);
		
		attributeNames = new JTextField();
		attributeNames.setFont(new Font("Tahoma", Font.PLAIN, 20));
		attributeNames.setBounds(28, 136, 947, 39);
		frame.getContentPane().add(attributeNames);
		attributeNames.setColumns(10);
		
		JLabel lblTableName = new JLabel("Table Name");
		lblTableName.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblTableName.setBounds(28, 65, 149, 20);
		frame.getContentPane().add(lblTableName);
		
		JLabel lblEnterTableAttributescomma = new JLabel("Enter table attributes(comma separated string)");
		lblEnterTableAttributescomma.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblEnterTableAttributescomma.setBounds(29, 100, 485, 20);
		frame.getContentPane().add(lblEnterTableAttributescomma);
		
		JButton btnInsertCountryData = new JButton("Insert Country Data");
		btnInsertCountryData.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnInsertCountryData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				readFile("country");
			}
		});
		btnInsertCountryData.setBounds(252, 236, 250, 29);
		frame.getContentPane().add(btnInsertCountryData);
		
		JLabel lblNewLabel = new JLabel("Find all cities whose population is more than 40% of the population of their entire country.");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblNewLabel.setBounds(28, 299, 868, 26);
		frame.getContentPane().add(lblNewLabel);
		
		JButton btnExecuteQuery = new JButton("Execute Query 1");
		btnExecuteQuery.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnExecuteQuery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				QueryExecution queryExecution = new QueryExecution();
			}
		});
		btnExecuteQuery.setBounds(124, 516, 196, 29);
		frame.getContentPane().add(btnExecuteQuery);

		JButton btnExecuteQuery2 = new JButton("Execute Query 2");
		btnExecuteQuery2.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnExecuteQuery2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				QueryExecution2 queryExecution = new QueryExecution2();
			}
		});
		btnExecuteQuery2.setBounds(612, 514, 184, 29);
		frame.getContentPane().add(btnExecuteQuery2);
		
		txtQueryExecutionPlan = new JTextArea();
		txtQueryExecutionPlan.setFont(new Font("Monospaced", Font.PLAIN, 14));
		txtQueryExecutionPlan.setWrapStyleWord(true);
		txtQueryExecutionPlan.setLineWrap(true);
		txtQueryExecutionPlan.setText("Query Execution Plan 1\r\n1) Join city and country on city.countrycode = country.code\r\n2) Result of join sent to Selection operator with \r\ncondition city.population > 0.4 * country.population\r\n3) Result of selection sent to Projection to select city.name");
		txtQueryExecutionPlan.setBounds(28, 337, 441, 163);
		frame.getContentPane().add(txtQueryExecutionPlan);
		txtQueryExecutionPlan.setColumns(10);
		
		txtQueryExecutionPlan2 = new JTextArea();
		txtQueryExecutionPlan2.setFont(new Font("Monospaced", Font.PLAIN, 14));
		txtQueryExecutionPlan2.setWrapStyleWord(true);
		txtQueryExecutionPlan2.setLineWrap(true);
		txtQueryExecutionPlan2.setText("Query Execution Plan 2\r\n1) Join city and country on city.countrycode = country.code \r\nand with condition city.population > 0.4 * country.population\r\n2) Result of selection sent to Projection to select city.name");
		txtQueryExecutionPlan2.setBounds(500, 337, 412, 163);
		frame.getContentPane().add(txtQueryExecutionPlan2);
		txtQueryExecutionPlan2.setColumns(10);
	}
	
	private void readFile(String filename){
		R = new Relation(filename,"");
		filename = "./src/" + filename + ".txt";
		String data = "";
		try {

		    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
		    String line;

		    while ((line = br.readLine()) != null) {
		    	R.insert(line);
		        //data += line;
		    }
		    br.close();

		} catch (IOException e) {
		    System.out.println("ERROR: unable to read file " + filename);
		    e.printStackTrace();   
		}
	}
}
