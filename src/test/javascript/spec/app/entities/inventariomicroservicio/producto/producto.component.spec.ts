import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { GatewaystoreTestModule } from '../../../../test.module';
import { ProductoComponent } from 'app/entities/inventariomicroservicio/producto/producto.component';
import { ProductoService } from 'app/entities/inventariomicroservicio/producto/producto.service';
import { Producto } from 'app/shared/model/inventariomicroservicio/producto.model';

describe('Component Tests', () => {
  describe('Producto Management Component', () => {
    let comp: ProductoComponent;
    let fixture: ComponentFixture<ProductoComponent>;
    let service: ProductoService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [GatewaystoreTestModule],
        declarations: [ProductoComponent]
      })
        .overrideTemplate(ProductoComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(ProductoComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(ProductoService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new Producto(123)],
            headers
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.productos && comp.productos[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
  });
});
