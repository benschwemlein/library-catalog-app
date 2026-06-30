import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Branch, BranchStats, CreateBranchRequest } from '../models/branch.model';

@Injectable({ providedIn: 'root' })
export class BranchService {
  private apiUrl = 'http://localhost:8080/api/branches';

  constructor(private http: HttpClient) {}

  getBranches(): Observable<Branch[]> {
    return this.http.get<Branch[]>(this.apiUrl);
  }

  getBranchById(id: number): Observable<Branch> {
    return this.http.get<Branch>(`${this.apiUrl}/${id}`);
  }

  getBranchInventory(branchId: number, page: number = 0, size: number = 20): Observable<any[]> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<any[]>(`${this.apiUrl}/${branchId}/inventory`, { params });
  }

  getBranchStats(branchId: number): Observable<BranchStats> {
    return this.http.get<BranchStats>(`${this.apiUrl}/${branchId}/stats`);
  }

  getAllBranchStats(): Observable<BranchStats[]> {
    return this.http.get<BranchStats[]>(`${this.apiUrl}/stats`);
  }

  createBranch(branch: CreateBranchRequest): Observable<Branch> {
    return this.http.post<Branch>(this.apiUrl, branch);
  }

  updateBranch(id: number, branch: Partial<CreateBranchRequest>): Observable<Branch> {
    return this.http.put<Branch>(`${this.apiUrl}/${id}`, branch);
  }

  deleteBranch(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
