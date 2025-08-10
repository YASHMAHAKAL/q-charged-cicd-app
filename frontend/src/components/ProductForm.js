import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Box,
  Typography,
  InputAdornment
} from '@mui/material';
import { useForm, Controller } from 'react-hook-form';

const ProductForm = ({ open, onClose, onSubmit, product, isEditing }) => {
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting }
  } = useForm({
    defaultValues: {
      name: product?.name || '',
      description: product?.description || '',
      price: product?.price || ''
    }
  });

  React.useEffect(() => {
    if (product) {
      reset({
        name: product.name || '',
        description: product.description || '',
        price: product.price || ''
      });
    } else {
      reset({
        name: '',
        description: '',
        price: ''
      });
    }
  }, [product, reset]);

  const handleFormSubmit = async (data) => {
    try {
      await onSubmit({
        ...data,
        price: parseFloat(data.price)
      });
      reset();
      onClose();
    } catch (error) {
      console.error('Form submission error:', error);
    }
  };

  const handleClose = () => {
    reset();
    onClose();
  };

  return (
    <Dialog 
      open={open} 
      onClose={handleClose} 
      maxWidth="sm" 
      fullWidth
      PaperProps={{
        sx: { borderRadius: 2 }
      }}
    >
      <DialogTitle>
        <Typography variant="h5" component="h2">
          {isEditing ? 'Edit Product' : 'Add New Product'}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {isEditing ? 'Update product information' : 'Fill in the details for the new product'}
        </Typography>
      </DialogTitle>

      <form onSubmit={handleSubmit(handleFormSubmit)}>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3, pt: 1 }}>
            <Controller
              name="name"
              control={control}
              rules={{
                required: 'Product name is required',
                minLength: {
                  value: 2,
                  message: 'Product name must be at least 2 characters'
                },
                maxLength: {
                  value: 100,
                  message: 'Product name cannot exceed 100 characters'
                }
              }}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Product Name"
                  variant="outlined"
                  fullWidth
                  error={!!errors.name}
                  helperText={errors.name?.message}
                  placeholder="Enter product name"
                />
              )}
            />

            <Controller
              name="description"
              control={control}
              rules={{
                maxLength: {
                  value: 500,
                  message: 'Description cannot exceed 500 characters'
                }
              }}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Description"
                  variant="outlined"
                  fullWidth
                  multiline
                  rows={4}
                  error={!!errors.description}
                  helperText={errors.description?.message}
                  placeholder="Enter product description"
                />
              )}
            />

            <Controller
              name="price"
              control={control}
              rules={{
                required: 'Price is required',
                min: {
                  value: 0.01,
                  message: 'Price must be greater than 0'
                },
                pattern: {
                  value: /^\d+(\.\d{1,2})?$/,
                  message: 'Please enter a valid price (e.g., 99.99)'
                }
              }}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Price"
                  variant="outlined"
                  fullWidth
                  type="number"
                  inputProps={{
                    step: "0.01",
                    min: "0"
                  }}
                  InputProps={{
                    startAdornment: <InputAdornment position="start">$</InputAdornment>,
                  }}
                  error={!!errors.price}
                  helperText={errors.price?.message}
                  placeholder="0.00"
                />
              )}
            />
          </Box>
        </DialogContent>

        <DialogActions sx={{ px: 3, pb: 3 }}>
          <Button 
            onClick={handleClose} 
            variant="outlined"
            disabled={isSubmitting}
          >
            Cancel
          </Button>
          <Button 
            type="submit" 
            variant="contained"
            disabled={isSubmitting}
            sx={{ minWidth: 100 }}
          >
            {isSubmitting ? 'Saving...' : (isEditing ? 'Update' : 'Create')}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default ProductForm;