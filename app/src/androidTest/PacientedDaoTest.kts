com.wdog.consultorioodontologico.database
@RunWith(AndroidJUnit4::class)
class PacienteDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: PacienteDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = database.pacienteDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertarPacienteYRecuperar() = runTest {
        val paciente = Paciente(
            nombre = "Juan",
            apellido = "Perez",
            edad = 30,
            fotos = emptyList(),
            historiaClinica = ""
        )
        dao.insert(paciente)
        val pacientes = dao.getAllPacientes().first()
        assertThat(pacientes).contains(paciente)
    }
}