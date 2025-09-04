export const mockAuthAPI = {
  login: async (credentials) => {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // Mock successful response
    return {
      data: {
        token: "mock-jwt-token-12345",
        userId: 1,
        name: "Test User",
        role: "CUSTOMER",
        email: credentials.email
      }
    };
    
    // Uncomment to simulate error
    // throw { 
    //   response: { 
    //     status: 401, 
    //     data: { error: "Invalid credentials" } 
    //   } 
    // };
  }
};