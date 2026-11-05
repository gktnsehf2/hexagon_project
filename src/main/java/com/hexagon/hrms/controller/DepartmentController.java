package com.hexagon.hrms.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hexagon.hrms.domain.Department;
import com.hexagon.hrms.dto.request.DepartmentFormDTO;
import com.hexagon.hrms.exception.BusinessException;
import com.hexagon.hrms.security.AccountDetails;
import com.hexagon.hrms.security.PermissionCode;
import com.hexagon.hrms.service.DepartmentService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/departments")
public class DepartmentController {

	private final DepartmentService departmentService;

	@GetMapping("")
	public String getTree(@RequestParam(value = "id", required = false) Integer id,
						  @RequestParam(value = "all", required = false) String all,
						  @AuthenticationPrincipal AccountDetails loginUser, Model model) {
		// 사용 안 함 조직은 부서 수정 권한이 있을 때만 볼 수 있음
		boolean includeInactive = "1".equals(all) && loginUser.hasAuthority(PermissionCode.DEPT_UPDATE);
		List<Department> tree = departmentService.getTree(includeInactive);

		Department selected = tree.isEmpty() ? null : tree.get(0);
		for (Department d : tree) {
			if (id != null && d.getDepartmentId() == id) {
				selected = d;
			}
		}
		List<Department> children = new ArrayList<>();
		Department parent = null;
		if (selected != null) {
			for (Department d : tree) {
				if (d.getParentId() != null && d.getParentId() == selected.getDepartmentId()) {
					children.add(d);
				}
				if (selected.getParentId() != null && d.getDepartmentId() == selected.getParentId()) {
					parent = d;
				}
			}
			model.addAttribute("members", departmentService.getMembers(selected.getDepartmentId()));
		}
		model.addAttribute("tree", tree);
		model.addAttribute("selected", selected);
		model.addAttribute("parent", parent);
		model.addAttribute("children", children);
		model.addAttribute("includeInactive", includeInactive);
		return "dept/list";
	}

	@GetMapping("/new")
	public String createForm(@RequestParam(value = "parentId", required = false) Integer parentId, Model model) {
		DepartmentFormDTO form = new DepartmentFormDTO();
		form.setParentId(parentId);
		model.addAttribute("form", form);
		addOptions(model);
		return "dept/form";
	}

	@PostMapping("/new")
	public String create(@ModelAttribute("form") DepartmentFormDTO form, Model model, RedirectAttributes ra) {
		try {
			int id = departmentService.create(form);
			ra.addFlashAttribute("message", "조직을 등록했습니다. 소속 사원을 옮긴 뒤 수정에서 조직장을 지정하세요.");
			return "redirect:/departments?id=" + id;
		} catch (BusinessException e) {
			model.addAttribute("formError", e.getMessage());
			addOptions(model);
			return "dept/form";
		}
	}

	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable("id") int id, Model model) {
		Department d = getDepartment(id);
		model.addAttribute("dept", d);
		model.addAttribute("form", DepartmentFormDTO.from(d));
		model.addAttribute("members", departmentService.getMembers(id));
		addOptions(model);
		return "dept/form";
	}

	@PostMapping("/{id}/edit")
	public String update(@PathVariable("id") int id, @ModelAttribute("form") DepartmentFormDTO form, Model model, RedirectAttributes ra) {
		Department d = getDepartment(id);
		try {
			departmentService.update(id, form);
			ra.addFlashAttribute("message", "조직 정보를 수정했습니다.");
			return "redirect:/departments?id=" + id + (form.getIsActive() == 0 ? "&all=1" : "");
		} catch (BusinessException e) {
			model.addAttribute("formError", e.getMessage());
			model.addAttribute("dept", d);
			model.addAttribute("members", departmentService.getMembers(id));
			addOptions(model);
			return "dept/form";
		}
	}

	private Department getDepartment(int id) {
		Department d = departmentService.get(id);
		if (d == null) {
			throw new BusinessException("존재하지 않는 조직입니다.");
		}
		return d;
	}

	private void addOptions(Model model) {
		List<Department> parents = new ArrayList<>();
		for (Department d : departmentService.getAll()) {
			if (d.getDepartmentLevel() < 3) {
				parents.add(d);
			}
		}
		model.addAttribute("parents", parents);
	}
}
