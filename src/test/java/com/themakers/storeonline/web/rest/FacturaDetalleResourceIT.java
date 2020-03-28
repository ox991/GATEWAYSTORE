package com.themakers.storeonline.web.rest;

import com.themakers.storeonline.GatewaystoreApp;
import com.themakers.storeonline.domain.FacturaDetalle;
import com.themakers.storeonline.repository.FacturaDetalleRepository;
import com.themakers.storeonline.web.rest.errors.ExceptionTranslator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

import static com.themakers.storeonline.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link FacturaDetalleResource} REST controller.
 */
@SpringBootTest(classes = GatewaystoreApp.class)
public class FacturaDetalleResourceIT {

    private static final Long DEFAULT_PRODUCTO_ID = 1L;
    private static final Long UPDATED_PRODUCTO_ID = 2L;

    private static final Long DEFAULT_CANTIDAD = 1L;
    private static final Long UPDATED_CANTIDAD = 2L;

    private static final BigDecimal DEFAULT_PRECIO_UNITARIO = new BigDecimal(1);
    private static final BigDecimal UPDATED_PRECIO_UNITARIO = new BigDecimal(2);

    @Autowired
    private FacturaDetalleRepository facturaDetalleRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restFacturaDetalleMockMvc;

    private FacturaDetalle facturaDetalle;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final FacturaDetalleResource facturaDetalleResource = new FacturaDetalleResource(facturaDetalleRepository);
        this.restFacturaDetalleMockMvc = MockMvcBuilders.standaloneSetup(facturaDetalleResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FacturaDetalle createEntity(EntityManager em) {
        FacturaDetalle facturaDetalle = new FacturaDetalle()
            .productoId(DEFAULT_PRODUCTO_ID)
            .cantidad(DEFAULT_CANTIDAD)
            .precioUnitario(DEFAULT_PRECIO_UNITARIO);
        return facturaDetalle;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FacturaDetalle createUpdatedEntity(EntityManager em) {
        FacturaDetalle facturaDetalle = new FacturaDetalle()
            .productoId(UPDATED_PRODUCTO_ID)
            .cantidad(UPDATED_CANTIDAD)
            .precioUnitario(UPDATED_PRECIO_UNITARIO);
        return facturaDetalle;
    }

    @BeforeEach
    public void initTest() {
        facturaDetalle = createEntity(em);
    }

    @Test
    @Transactional
    public void createFacturaDetalle() throws Exception {
        int databaseSizeBeforeCreate = facturaDetalleRepository.findAll().size();

        // Create the FacturaDetalle
        restFacturaDetalleMockMvc.perform(post("/api/factura-detalles")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(facturaDetalle)))
            .andExpect(status().isCreated());

        // Validate the FacturaDetalle in the database
        List<FacturaDetalle> facturaDetalleList = facturaDetalleRepository.findAll();
        assertThat(facturaDetalleList).hasSize(databaseSizeBeforeCreate + 1);
        FacturaDetalle testFacturaDetalle = facturaDetalleList.get(facturaDetalleList.size() - 1);
        assertThat(testFacturaDetalle.getProductoId()).isEqualTo(DEFAULT_PRODUCTO_ID);
        assertThat(testFacturaDetalle.getCantidad()).isEqualTo(DEFAULT_CANTIDAD);
        assertThat(testFacturaDetalle.getPrecioUnitario()).isEqualTo(DEFAULT_PRECIO_UNITARIO);
    }

    @Test
    @Transactional
    public void createFacturaDetalleWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = facturaDetalleRepository.findAll().size();

        // Create the FacturaDetalle with an existing ID
        facturaDetalle.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restFacturaDetalleMockMvc.perform(post("/api/factura-detalles")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(facturaDetalle)))
            .andExpect(status().isBadRequest());

        // Validate the FacturaDetalle in the database
        List<FacturaDetalle> facturaDetalleList = facturaDetalleRepository.findAll();
        assertThat(facturaDetalleList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllFacturaDetalles() throws Exception {
        // Initialize the database
        facturaDetalleRepository.saveAndFlush(facturaDetalle);

        // Get all the facturaDetalleList
        restFacturaDetalleMockMvc.perform(get("/api/factura-detalles?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(facturaDetalle.getId().intValue())))
            .andExpect(jsonPath("$.[*].productoId").value(hasItem(DEFAULT_PRODUCTO_ID.intValue())))
            .andExpect(jsonPath("$.[*].cantidad").value(hasItem(DEFAULT_CANTIDAD.intValue())))
            .andExpect(jsonPath("$.[*].precioUnitario").value(hasItem(DEFAULT_PRECIO_UNITARIO.intValue())));
    }
    
    @Test
    @Transactional
    public void getFacturaDetalle() throws Exception {
        // Initialize the database
        facturaDetalleRepository.saveAndFlush(facturaDetalle);

        // Get the facturaDetalle
        restFacturaDetalleMockMvc.perform(get("/api/factura-detalles/{id}", facturaDetalle.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(facturaDetalle.getId().intValue()))
            .andExpect(jsonPath("$.productoId").value(DEFAULT_PRODUCTO_ID.intValue()))
            .andExpect(jsonPath("$.cantidad").value(DEFAULT_CANTIDAD.intValue()))
            .andExpect(jsonPath("$.precioUnitario").value(DEFAULT_PRECIO_UNITARIO.intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingFacturaDetalle() throws Exception {
        // Get the facturaDetalle
        restFacturaDetalleMockMvc.perform(get("/api/factura-detalles/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateFacturaDetalle() throws Exception {
        // Initialize the database
        facturaDetalleRepository.saveAndFlush(facturaDetalle);

        int databaseSizeBeforeUpdate = facturaDetalleRepository.findAll().size();

        // Update the facturaDetalle
        FacturaDetalle updatedFacturaDetalle = facturaDetalleRepository.findById(facturaDetalle.getId()).get();
        // Disconnect from session so that the updates on updatedFacturaDetalle are not directly saved in db
        em.detach(updatedFacturaDetalle);
        updatedFacturaDetalle
            .productoId(UPDATED_PRODUCTO_ID)
            .cantidad(UPDATED_CANTIDAD)
            .precioUnitario(UPDATED_PRECIO_UNITARIO);

        restFacturaDetalleMockMvc.perform(put("/api/factura-detalles")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedFacturaDetalle)))
            .andExpect(status().isOk());

        // Validate the FacturaDetalle in the database
        List<FacturaDetalle> facturaDetalleList = facturaDetalleRepository.findAll();
        assertThat(facturaDetalleList).hasSize(databaseSizeBeforeUpdate);
        FacturaDetalle testFacturaDetalle = facturaDetalleList.get(facturaDetalleList.size() - 1);
        assertThat(testFacturaDetalle.getProductoId()).isEqualTo(UPDATED_PRODUCTO_ID);
        assertThat(testFacturaDetalle.getCantidad()).isEqualTo(UPDATED_CANTIDAD);
        assertThat(testFacturaDetalle.getPrecioUnitario()).isEqualTo(UPDATED_PRECIO_UNITARIO);
    }

    @Test
    @Transactional
    public void updateNonExistingFacturaDetalle() throws Exception {
        int databaseSizeBeforeUpdate = facturaDetalleRepository.findAll().size();

        // Create the FacturaDetalle

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFacturaDetalleMockMvc.perform(put("/api/factura-detalles")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(facturaDetalle)))
            .andExpect(status().isBadRequest());

        // Validate the FacturaDetalle in the database
        List<FacturaDetalle> facturaDetalleList = facturaDetalleRepository.findAll();
        assertThat(facturaDetalleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteFacturaDetalle() throws Exception {
        // Initialize the database
        facturaDetalleRepository.saveAndFlush(facturaDetalle);

        int databaseSizeBeforeDelete = facturaDetalleRepository.findAll().size();

        // Delete the facturaDetalle
        restFacturaDetalleMockMvc.perform(delete("/api/factura-detalles/{id}", facturaDetalle.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<FacturaDetalle> facturaDetalleList = facturaDetalleRepository.findAll();
        assertThat(facturaDetalleList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
