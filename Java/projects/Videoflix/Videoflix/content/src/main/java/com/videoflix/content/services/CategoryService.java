package com.videoflix.content.services;

import com.videoflix.content.entities.Category;
import com.videoflix.content.repositories.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CategoryService {

    // Messages d'erreur
    private static final String CATEGORY_NOT_FOUND_BY_ID = "Catégorie non trouvée avec l'ID : ";
    private static final String CATEGORY_NOT_FOUND_BY_NAME = "Catégorie non trouvée avec le nom : ";
    private static final String CATEGORY_ALREADY_EXISTS = "Une catégorie avec le nom '%s' existe déjà.";
    private static final String CATEGORY_NAME_CANNOT_BE_EMPTY = "Le nom de la catégorie ne peut pas être vide";
    private static final String CATEGORY_NAME_TOO_LONG = "Le nom de la catégorie ne peut pas dépasser 100 caractères";
    private static final String CATEGORY_CANNOT_BE_NULL = "La catégorie ne peut pas être null";
    private static final String INVALID_ID = "L'ID doit être un nombre positif";
    private static final String INVALID_DATA_ERROR = "Erreur lors de la création de la catégorie : données invalides";
    private static final String UPDATE_DATA_ERROR = "Erreur lors de la mise à jour de la catégorie : données invalides";
    private static final String DELETE_CONSTRAINT_ERROR = "Impossible de supprimer la catégorie : elle est référencée par d'autres entités";
    private static final String DELETE_MULTIPLE_CONSTRAINT_ERROR = "Impossible de supprimer certaines catégories : elles sont référencées par d'autres entités";
    private static final String SOME_CATEGORIES_NOT_FOUND = "Certaines catégories n'ont pas été trouvées";
    private static final String INVALID_PAGINATION_PARAMS = "Les paramètres de pagination doivent être positifs";

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Crée une nouvelle catégorie dans la base de données.
     * 
     * @param category L'objet Category à sauvegarder.
     * @return La catégorie sauvegardée, avec son ID généré.
     * @throws IllegalArgumentException si les données sont invalides ou si la
     *                                  catégorie existe déjà.
     */
    @Transactional
    public Category createCategory(Category category) {
        validateCategoryData(category);

        if (categoryRepository.existsByNameIgnoreCase(category.getName().trim())) {
            throw new IllegalArgumentException(String.format(CATEGORY_ALREADY_EXISTS, category.getName()));
        }

        // Normaliser le nom avant sauvegarde
        category.setName(category.getName().trim());
        if (category.getDescription() != null) {
            category.setDescription(category.getDescription().trim());
        }

        try {
            return categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(INVALID_DATA_ERROR, e);
        }
    }

    /**
     * Récupère une catégorie par son ID.
     * 
     * @param id L'ID de la catégorie à récupérer.
     * @return La catégorie correspondante.
     * @throws IllegalArgumentException si l'ID est invalide.
     * @throws EntityNotFoundException  si aucune catégorie n'est trouvée avec cet
     *                                  ID.
     */
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        validateId(id);
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(CATEGORY_NOT_FOUND_BY_ID + id));
    }

    /**
     * Récupère une catégorie par son nom (insensible à la casse).
     * 
     * @param name Le nom de la catégorie à récupérer.
     * @return La catégorie correspondante.
     * @throws IllegalArgumentException si le nom est invalide.
     * @throws EntityNotFoundException  si aucune catégorie n'est trouvée avec ce
     *                                  nom.
     */
    @Transactional(readOnly = true)
    public Category getCategoryByName(String name) {
        validateName(name);
        return categoryRepository.findByNameIgnoreCase(name.trim())
                .orElseThrow(() -> new EntityNotFoundException(CATEGORY_NOT_FOUND_BY_NAME + name));
    }

    /**
     * Vérifie si une catégorie existe par son nom.
     * 
     * @param name Le nom de la catégorie.
     * @return true si la catégorie existe, false sinon.
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        if (!StringUtils.hasText(name)) {
            return false;
        }
        return categoryRepository.existsByNameIgnoreCase(name.trim());
    }

    /**
     * Récupère toutes les catégories existantes, triées par nom.
     * 
     * @return Une liste de toutes les catégories triées par nom.
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    /**
     * Récupère les catégories avec pagination.
     * 
     * @param page Numéro de page (0-indexé).
     * @param size Taille de la page.
     * @return Liste paginée des catégories.
     */
    @Transactional(readOnly = true)
    public List<Category> getCategoriesPaginated(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException(INVALID_PAGINATION_PARAMS);
        }
        // Note: Pour une pagination complète, utilisez Page<Category> et Pageable
        return categoryRepository.findAll().stream()
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    /**
     * Met à jour une catégorie existante.
     * 
     * @param id              L'ID de la catégorie à mettre à jour.
     * @param updatedCategory L'objet Category contenant les nouvelles informations.
     * @return La catégorie mise à jour.
     * @throws IllegalArgumentException si les données sont invalides.
     * @throws EntityNotFoundException  si aucune catégorie n'est trouvée avec cet
     *                                  ID.
     */
    @Transactional
    public Category updateCategory(Long id, Category updatedCategory) {
        validateId(id);
        validateCategoryData(updatedCategory);

        // Récupération directe sans appel à la méthode transactionnelle
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(CATEGORY_NOT_FOUND_BY_ID + id));
        String newName = updatedCategory.getName().trim();

        // Vérifier si le nouveau nom est déjà pris par une AUTRE catégorie
        if (!existingCategory.getName().equalsIgnoreCase(newName) &&
                categoryRepository.existsByNameIgnoreCase(newName)) {
            throw new IllegalArgumentException(String.format(CATEGORY_ALREADY_EXISTS, newName));
        }

        existingCategory.setName(newName);
        if (updatedCategory.getDescription() != null) {
            existingCategory.setDescription(updatedCategory.getDescription().trim());
        }

        try {
            return categoryRepository.save(existingCategory);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(UPDATE_DATA_ERROR, e);
        }
    }

    /**
     * Met à jour partiellement une catégorie.
     * 
     * @param id          L'ID de la catégorie à mettre à jour.
     * @param name        Nouveau nom (optionnel).
     * @param description Nouvelle description (optionnel).
     * @return La catégorie mise à jour.
     */
    @Transactional
    public Category partialUpdateCategory(Long id, String name, String description) {
        validateId(id);

        // Récupération directe sans appel à la méthode transactionnelle
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(CATEGORY_NOT_FOUND_BY_ID + id));

        if (StringUtils.hasText(name)) {
            String trimmedName = name.trim();
            if (!existingCategory.getName().equalsIgnoreCase(trimmedName) &&
                    categoryRepository.existsByNameIgnoreCase(trimmedName)) {
                throw new IllegalArgumentException(String.format(CATEGORY_ALREADY_EXISTS, trimmedName));
            }
            existingCategory.setName(trimmedName);
        }

        if (description != null) {
            existingCategory.setDescription(description.trim());
        }

        return categoryRepository.save(existingCategory);
    }

    /**
     * Supprime une catégorie par son ID.
     * 
     * @param id L'ID de la catégorie à supprimer.
     * @throws IllegalArgumentException si l'ID est invalide.
     * @throws EntityNotFoundException  si aucune catégorie n'est trouvée avec cet
     *                                  ID.
     */
    @Transactional
    public void deleteCategory(Long id) {
        validateId(id);

        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException(CATEGORY_NOT_FOUND_BY_ID + id);
        }

        try {
            categoryRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(DELETE_CONSTRAINT_ERROR, e);
        }
    }

    /**
     * Supprime plusieurs catégories par leurs IDs.
     * 
     * @param ids Liste des IDs des catégories à supprimer.
     * @return Nombre de catégories supprimées.
     */
    @Transactional
    public int deleteCategoriesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        ids.forEach(this::validateId);
        List<Category> categoriesToDelete = categoryRepository.findAllById(ids);

        if (categoriesToDelete.size() != ids.size()) {
            throw new EntityNotFoundException(SOME_CATEGORIES_NOT_FOUND);
        }

        try {
            categoryRepository.deleteAllById(ids);
            return categoriesToDelete.size();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(DELETE_MULTIPLE_CONSTRAINT_ERROR, e);
        }
    }

    /**
     * Compte le nombre total de catégories.
     * 
     * @return Le nombre de catégories.
     */
    @Transactional(readOnly = true)
    public long countCategories() {
        return categoryRepository.count();
    }

    // Méthodes de validation privées

    private void validateCategoryData(Category category) {
        if (category == null) {
            throw new IllegalArgumentException(CATEGORY_CANNOT_BE_NULL);
        }
        validateName(category.getName());
    }

    private void validateName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException(CATEGORY_NAME_CANNOT_BE_EMPTY);
        }
        if (name.trim().length() > 100) { // Assumant une limite de 100 caractères
            throw new IllegalArgumentException(CATEGORY_NAME_TOO_LONG);
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(INVALID_ID);
        }
    }
}