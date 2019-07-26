import { TestBed } from '@angular/core/testing';

import { GenericHttpService } from './generic-http.service';

describe('GenericHttpService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: GenericHttpService = TestBed.get(GenericHttpService);
    expect(service).toBeTruthy();
  });
});
